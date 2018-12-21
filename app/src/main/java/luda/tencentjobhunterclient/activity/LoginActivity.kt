package luda.tencentjobhunterclient.activity

import TencentJobHunterMessage.Message
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.act_login.*
import luda.tencentjobhunterclient.R
import luda.tencentjobhunterclient.application.MyApplication
import luda.tencentjobhunterclient.exception.GetResponseException
import luda.tencentjobhunterclient.option.ConnectionOption
import luda.tencentjobhunterclient.util.LoginHelper
import luda.tencentjobhunterclient.util.SettingHelper
import luda.tencentjobhunterclient.util.mySubscribe
import java.lang.Exception
import java.util.*

/**
 * 登录/注册活动
 */
class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private lateinit var mRealm: Realm



    override fun onCreate(savedInstanceState: Bundle?) {

        mRealm = RealmHelper.getInstance()
        //attemptAutoLogin()

        super.onCreate(savedInstanceState)


        // 设置登录界面
        setContentView(R.layout.act_login)


        tbx_password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                //attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        btn_commit.setOnClickListener {
            if(rb_register.isChecked)
            {
                attemptRegister()
            }
            else
            {
                attemptLogin()
            }
        }

        rb_register.setOnCheckedChangeListener { _, flag ->
            til_password_repeat.visibility = if(flag) View.VISIBLE else View.GONE
            til_email.visibility = if(flag) View.VISIBLE else View.GONE
            btn_commit.text = if(flag) "注册" else "登录"
        }

        tbx_server.setText("${SettingHelper.serverIp}:${SettingHelper.serverPort}")

    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
        MyApplication.refWatcher.watch(this)
    }


    private fun doRegister(username:String,password:String,email:String,option:ConnectionOption)
    {
        LoginHelper.register(username,password,email,option)
                .mySubscribe(this,true,{result->
                    intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },{e->
                    when((e as GetResponseException).errorCode)
                    {
                        Message.ErrorCode.REGISTER_ALREADY_EXISTS->Toast.makeText(this,"用户名已存在",Toast.LENGTH_LONG).show()
                        else ->Toast.makeText(this,"未知错误！",Toast.LENGTH_LONG).show()
                    }
                })

    }

    private fun doLogin(username:String,password:String,option:ConnectionOption)
    {
        LoginHelper.login(username,password,option)
                .mySubscribe(this,true,{result->
                    intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },{e->
                    when((e as GetResponseException).errorCode)
                    {
                        Message.ErrorCode.LOGIN_INCORRECT_PASS->Toast.makeText(this,"用户名或密码错误",Toast.LENGTH_LONG).show()
                    }
                })

    }

    /**
     * 尝试注册或登录
     * 检查输入是否有误
     */

    private fun attemptRegister()
    {

        // 重置error
        tbx_email.error = null
        tbx_password.error = null

        var username = tbx_username.text.toString()
        val passwordStr = tbx_password.text.toString()
        val passwordStr2 = tbx_password.text.toString()
        val emailStr = tbx_email.text.toString()

        var cancel = false
        var focusView: View? = null

        //检查用户名格式是否合法
        if (!isUsernameValid(username)) {
            tbx_username.error = getString(R.string.error_invalid_username)
            focusView = tbx_username
            return
        }

        // 检查密码格式是否合法
        if (!isPasswordValid(passwordStr)) {
            tbx_password.error = getString(R.string.error_invalid_password)
            focusView = tbx_password
            return
        }

        if (passwordStr!=passwordStr2)
        {
            tbx_password_repeat.error = getString(R.string.error_incorrect_repeat)
            focusView = tbx_password_repeat
        }

        // 检查邮箱格式是否合法
        if (!isEmailValid(emailStr)) {
            tbx_password.error = getString(R.string.error_invalid_password)
            focusView = tbx_email
            return
        }

        val option = getConnectionOption()
        if(option==null)
        {
            tbx_server.error = "服务器格式非法"
            focusView = tbx_server
            return
        }

        doRegister(username,passwordStr,emailStr,option)
    }

    private fun getConnectionOption() : ConnectionOption?
    {
        try {
            val args = tbx_server.text.split(':')

            val regex = Regex("^[a-zA-Z0-9_\\.]+[a-zA-Z0-9]$")
            if(!regex.matches(args[0]))return null

            return ConnectionOption(args[0],args[1].toInt())
        }
        catch (ex:Exception)
        {
            return null
        }
    }


    private fun attemptLogin() {

        // 重置error
        tbx_email.error = null
        tbx_password.error = null

        var username = tbx_username.text.toString()
        val passwordStr = tbx_password.text.toString()
        //val emailStr = email.text.toString()

        var cancel = false
        var focusView: View? = null

        // 检查密码格式是否合法
        if (TextUtils.isEmpty(passwordStr)) {
            tbx_password.error = "密码不能为空"
            focusView = tbx_password
            return
        }

        val option = getConnectionOption()
        if(option==null)
        {
            tbx_server.error = "服务器格式非法"
            focusView = tbx_server
            return
        }

        doLogin(username,passwordStr,option)





        // 检查邮箱格式是否合法
/*        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }*/

//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView?.requestFocus()
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true)
//            mAuthTask = UserLoginTask(emailStr, passwordStr)
//            mAuthTask!!.execute(null as Void?)
//        }
    }

    private fun isUsernameValid(username: String): Boolean {
        //用正则表达式判断邮箱格式是否合法
        val usernameRegex = Regex("^\\w+$")
        return username.length>=2 && usernameRegex.matches(username)
    }

    private fun isEmailValid(email: String): Boolean {
        //用正则表达式判断邮箱格式是否合法
        val emailRegex = Regex("^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$")
        return  emailRegex.matches(email)
    }

    private fun isPasswordValid(password: String): Boolean {
        //长度至少为4
        return password.length >= 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {

        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        tbx_email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }
}

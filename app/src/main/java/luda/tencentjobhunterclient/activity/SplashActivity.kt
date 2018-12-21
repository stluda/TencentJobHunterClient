package luda.tencentjobhunterclient.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import luda.tencentjobhunterclient.util.LoginHelper

/**
 * Created by luda on 2018/7/8
 * QQ 340071887.
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this,
                if(LoginHelper.isLoggedIn)
                    MainActivity::class.java
                else
                    LoginActivity::class.java)

        startActivity(intent)
        finish()
    }

}
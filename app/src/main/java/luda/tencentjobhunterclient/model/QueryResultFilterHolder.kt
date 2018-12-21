package luda.tencentjobhunterclient.model

/**
 * Created by luda on 2018/6/28
 * QQ 340071887.
 */
class QueryResultFilterHolder {

    private val typeFilterSet = HashSet<String>()
    private val locationFilterSet = HashSet<String>()

    var enableUnreadOnly : Boolean = false

    val isEmpty : Boolean
        get() = !enableUnreadOnly && typeFilterSet.isEmpty() && locationFilterSet.isEmpty()

    fun satisfy(queryResult: IQueryResultSource, index:Int) : Boolean{

        if(enableUnreadOnly&&queryResult.getItemAt(index).hasBeenSeen)
            return false

        if(typeFilterSet.isEmpty()&&locationFilterSet.isEmpty())
            return true

        val job = queryResult.getItemAt(index)
        for(type in typeFilterSet){
            if(type==job.type)return true
        }

        for (location in locationFilterSet){
            if(location==job.location)return true
        }

        return false
    }

    fun addTypeFilter(type: String){
        typeFilterSet.add(type)
    }

    fun addLocationFilter(location:String){
        locationFilterSet.add(location)
    }

    fun removeTypeFilter(type: String){
        typeFilterSet.remove(type)
    }

    fun removeLocationFilter(type: String){
        locationFilterSet.remove(type)
    }



}
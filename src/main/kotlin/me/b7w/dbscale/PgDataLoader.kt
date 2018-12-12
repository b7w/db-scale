package me.b7w.dbscale

class PgDataLoader(val count: Long) {

    suspend fun load(): String {
        return "Success $count"
    }

}

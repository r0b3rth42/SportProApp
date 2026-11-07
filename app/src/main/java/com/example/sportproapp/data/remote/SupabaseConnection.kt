package com.example.sportproapp.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://lbtfgmgtkgbcwbwsrhfs.supabase.co",
    supabaseKey = "sb_publishable_4cDdBbvyY1CbRyUZgrzRyQ_d4itcjjm"
) {
    install(Postgrest)
}
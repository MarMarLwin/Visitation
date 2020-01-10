package com.onthego.onthegovisitation.Common

import android.content.Context
import android.os.Build
import com.onthego.onthegovisitation.GeneralClass
import com.onthego.onthegovisitation.Utils
import java.util.*

class LocaleHelper
{
    companion object
    {
        var mEnglish = "en"
        var mBurmaZawgyi = "my"
        var mBurmaUnicode = "my"
        fun onAttach(context : Context) : Context
        {
            val language = getPersistLanguage(context)
            return updateResources(context, language)
        }

        fun getLanguage(context : Context) : String
        {
            return getPersistLanguage(context)
        }

        private fun persistLanguage(context : Context, language : String)
        {
            Utils.savePreference(context, GeneralClass.selectedLanguage, language)
        }

        private fun getPersistLanguage(context : Context) : String
        {
            return Utils.getPreference(context, GeneralClass.selectedLanguage).toString()
        }

        fun setLocale(context : Context, language : String,id:String)
        {
            persistLanguage(context, language)
            updateResources(context, language)
            Utils.savePreference(context,GeneralClass.selectedLanguageID,id)
        }

        private fun updateResources(context : Context, language : String) : Context
        {
            var ctx = context
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            val resource = context.resources

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N)
            {
                config.setLocale(locale)
                ctx = context.createConfigurationContext(config)
            }
            else
            {
                config.locale = locale
                resource.updateConfiguration(config, resource.displayMetrics)
            }
            return ctx
        }
    }
}
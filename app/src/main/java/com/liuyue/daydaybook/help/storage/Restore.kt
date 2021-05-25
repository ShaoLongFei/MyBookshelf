package com.liuyue.daydaybook.help.storage

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.liuyue.daydaybook.DbHelper
import com.liuyue.daydaybook.MApplication
import com.liuyue.daydaybook.R
import com.liuyue.daydaybook.base.observer.MySingleObserver
import com.liuyue.daydaybook.bean.*
import com.liuyue.daydaybook.help.FileHelp
import com.liuyue.daydaybook.help.LauncherIcon
import com.liuyue.daydaybook.help.ReadBookControl
import com.liuyue.daydaybook.model.BookSourceManager
import com.liuyue.daydaybook.model.ReplaceRuleManager
import com.liuyue.daydaybook.model.TxtChapterRuleManager
import com.liuyue.daydaybook.utils.DocumentUtil
import com.liuyue.daydaybook.utils.GSON
import com.liuyue.daydaybook.utils.fromJsonArray
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

object Restore {

    fun restore(context: Context, uri: Uri, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            DocumentFile.fromTreeUri(context, uri)?.listFiles()?.forEach { doc ->
                for (fileName in Backup.backupFileNames) {
                    if (doc.name == fileName) {
                        DocumentUtil.readBytes(context, doc.uri)?.let {
                            FileHelp.createFileIfNotExist(Backup.backupPath + File.separator + fileName)
                                    .writeBytes(it)
                        }
                    }
                }
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        restore(Backup.backupPath, callBack)
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }

    fun restore(path: String, callBack: CallBack?) {
        Single.create(SingleOnSubscribe<Boolean> { e ->
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookShelf.json")
                val json = file.readText()
                GSON.fromJsonArray<BookShelfBean>(json)?.forEach { bookshelf ->
                    if (bookshelf.noteUrl != null) {
                        DbHelper.getDaoSession().bookShelfBeanDao.insertOrReplace(bookshelf)
                    }
                    if (bookshelf.bookInfoBean.noteUrl != null) {
                        DbHelper.getDaoSession().bookInfoBeanDao.insertOrReplace(bookshelf.bookInfoBean)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSource.json")
                val json = file.readText()
                GSON.fromJsonArray<BookSourceBean>(json)?.let {
                    BookSourceManager.addBookSource(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookSearchHistory.json")
                val json = file.readText()
                GSON.fromJsonArray<SearchHistoryBean>(json)?.let {
                    DbHelper.getDaoSession().searchHistoryBeanDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myBookReplaceRule.json")
                val json = file.readText()
                GSON.fromJsonArray<ReplaceRuleBean>(json)?.let {
                    ReplaceRuleManager.addDataS(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val file = FileHelp.createFileIfNotExist(path + File.separator + "myTxtChapterRule.json")
                val json = file.readText()
                GSON.fromJsonArray<TxtChapterRuleBean>(json)?.let {
                    TxtChapterRuleManager.save(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var donateHb = MApplication.getConfigPreferences().getLong("DonateHb", 0)
            donateHb = if (donateHb > System.currentTimeMillis()) 0 else donateHb
            Preferences.getSharedPreferences(MApplication.getInstance(), path, "config")?.all?.map {
                val edit = MApplication.getConfigPreferences().edit()
                when (val value = it.value) {
                    is Int -> edit.putInt(it.key, value)
                    is Boolean -> edit.putBoolean(it.key, value)
                    is Long -> edit.putLong(it.key, value)
                    is Float -> edit.putFloat(it.key, value)
                    is String -> edit.putString(it.key, value)
                    else -> Unit
                }
                edit.putLong("DonateHb", donateHb)
                edit.putInt("versionCode", MApplication.getVersionCode())
                edit.apply()
            }
            e.onSuccess(true)
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : MySingleObserver<Boolean>() {
                    override fun onSuccess(t: Boolean) {
                        LauncherIcon.ChangeIcon(MApplication.getConfigPreferences().getString("launcher_icon", MApplication.getInstance().getString(R.string.icon_main)))
                        ReadBookControl.getInstance().updateReaderSettings()
                        MApplication.getInstance().upThemeStore()
                        MApplication.getInstance().initNightTheme()
                        callBack?.restoreSuccess()
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        callBack?.restoreError(e.localizedMessage ?: "ERROR")
                    }
                })
    }


    interface CallBack {
        fun restoreSuccess()
        fun restoreError(msg: String)
    }

}
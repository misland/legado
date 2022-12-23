@file:Suppress("DEPRECATION")

package io.legado.app.ui.main

import android.os.Bundle
import android.text.format.DateUtils
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import io.legado.app.BuildConfig
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.constant.AppConst.appInfo
import io.legado.app.constant.EventBus
import io.legado.app.constant.PreferKey
import io.legado.app.databinding.ActivityMainBinding
import io.legado.app.help.AppWebDav
import io.legado.app.help.BookHelp
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.LocalConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.help.storage.Backup
import io.legado.app.lib.dialogs.alert
import io.legado.app.service.BaseReadAloudService
import io.legado.app.ui.widget.dialog.TextDialog
import io.legado.app.utils.*
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 主界面
 */
class MainActivity : VMBaseActivity<ActivityMainBinding, MainViewModel>() {

    val TAG: String = "||========>DEBUG-MainActivity"
    override val binding by viewBinding(ActivityMainBinding::inflate)
    override val viewModel by viewModels<MainViewModel>()
    private var exitTime: Long = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        upVersion()
        privacyPolicy()
        syncAlert()
    }

    private fun upVersion() {
        if (LocalConfig.versionCode != appInfo.versionCode) {
            LocalConfig.versionCode = appInfo.versionCode
            if (LocalConfig.isFirstOpenApp) {
                val help = String(assets.open("help/appHelp.md").readBytes())
                showDialogFragment(TextDialog(help, TextDialog.Mode.MD))
            } else if (!BuildConfig.DEBUG) {
                val log = String(assets.open("updateLog.md").readBytes())
                showDialogFragment(TextDialog(log, TextDialog.Mode.MD))
            }
            viewModel.upVersion()
        }
    }

    /**
     * 同步提示
     */
    private fun syncAlert() = launch {
        val lastBackupFile = withContext(IO) { AppWebDav.lastBackUp().getOrNull() }
            ?: return@launch
        if (lastBackupFile.lastModify - LocalConfig.lastBackup > DateUtils.MINUTE_IN_MILLIS) {
            LocalConfig.lastBackup = lastBackupFile.lastModify
            alert("恢复", "webDav书源比本地新,是否恢复") {
                cancelButton()
                okButton {
                    viewModel.restoreWebDav(lastBackupFile.displayName)
                }
            }
        }
    }

    /**
     * 用户隐私与协议
     */
    private fun privacyPolicy() {
        if (LocalConfig.privacyPolicyOk) return
        val privacyPolicy = String(assets.open("privacyPolicy.md").readBytes())
        alert("用户隐私与协议", privacyPolicy) {
            noButton()
            yesButton {
                LocalConfig.privacyPolicyOk = true
            }
            onCancelled {
                finish()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> if (event.isTracking && !event.isCanceled) {
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        toastOnUi(R.string.double_click_exit)
                        exitTime = System.currentTimeMillis()
                    } else {
                        if (BaseReadAloudService.pause) {
                            finish()
                        } else {
                            moveTaskToBack(true)
                        }
                    }
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (AppConfig.autoRefreshBook) {
            outState.putBoolean("isAutoRefreshedBook", true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Coroutine.async {
            BookHelp.clearInvalidCache()
        }
        if (!BuildConfig.DEBUG) {
            Backup.autoBack(this)
        }
    }

    override fun observeLiveBus() {
        observeEvent<String>(EventBus.RECREATE) {
            recreate()
        }
        observeEvent<String>(PreferKey.threadCount) {
            viewModel.upPool()
        }
    }


}
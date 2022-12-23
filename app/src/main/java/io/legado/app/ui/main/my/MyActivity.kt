package io.legado.app.ui.main.my

import android.os.Bundle
import androidx.activity.viewModels
import io.legado.app.base.VMBaseActivity
import io.legado.app.databinding.ActivityMyBinding
import io.legado.app.utils.viewbindingdelegate.viewBinding

class MyActivity : VMBaseActivity<ActivityMyBinding, MyViewModel>() {

    val TAG: String = "||========>DEBUG-MyActivity"
    override val binding by viewBinding(ActivityMyBinding::inflate)
    override val viewModel by viewModels<MyViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {

    }

}
@file:Suppress("RedundantVisibilityModifier", "unused")

package io.legado.app.utils.viewbindingdelegate

import android.view.LayoutInflater
import androidx.core.app.ComponentActivity
import androidx.viewbinding.ViewBinding

/**
 * Create new [ViewBinding] associated with the [ComponentActivity]
 */
@JvmName("viewBindingActivity")
inline fun <T : ViewBinding> ComponentActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
    setContentView: Boolean = false
) = lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    // XxxBinding.inflate(LayoutInflater)
    // 作用是加载布局，但是并未与Activity绑定，绑定需要使用下面的setContentView
    val binding = bindingInflater.invoke(layoutInflater)
    if (setContentView) {
        setContentView(binding.root)
    }
    binding
}

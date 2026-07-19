package dev.jekis.canvacompass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dev.jekis.canvacompass.presentation.CompassViewModel
import dev.jekis.canvacompass.ui.CompassScreen
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ComposeFragment : Fragment() {

    private val viewModel: CompassViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    CompassScreen(viewModel = viewModel)
                }
            }
        }
    }
}
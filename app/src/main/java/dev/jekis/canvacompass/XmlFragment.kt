package dev.jekis.canvacompass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.jekis.canvacompass.databinding.FragmentXmlBinding
import dev.jekis.canvacompass.presentation.CompassViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class XmlFragment : Fragment() {

    private var _binding: FragmentXmlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompassViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXmlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var firstUpdate = true
                viewModel.uiState.collect { state ->
                    binding.compassView.setAzimuth(state.azimuth)
                    if (firstUpdate) {
                        requireActivity().reportFullyDrawn()
                        firstUpdate = false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
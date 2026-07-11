package dev.jekis.canvacompass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import dev.jekis.canvacompass.databinding.FragmentXmlBinding

private const val LISTENER_KEY = "XmlFragmentListenerKey"
class XmlFragment : Fragment() {

    private var _binding: FragmentXmlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: XmlViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXmlBinding.inflate(inflater, container, false)
        val fragmentListener = { requestKey: String, bundle: Bundle ->
            val args = bundle.get(requestKey)
            println(args)
        }
        setFragmentResultListener(requestKey = LISTENER_KEY, fragmentListener)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
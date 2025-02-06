package com.example.composition.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.composition.R
import com.example.composition.databinding.FragmentGameFinishedBinding
import com.example.composition.domain.entity.GameResult

class GameFinishedFragment:Fragment() {

    private val args by navArgs<GameFinishedFragmentArgs>()

    private var _binding: FragmentGameFinishedBinding? = null
    private val binding: FragmentGameFinishedBinding
        get() = _binding ?: throw RuntimeException("FragmentGameFinishedBinding == null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameFinishedBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonRetry.setOnClickListener {
            retryGame()
        }
        bindViews()
    }

    private fun bindViews(){
        binding.emojiResult.setImageResource(getSmileResId())
        binding.tvRequiredAnswers.text = String.format(
            getString(R.string.required_score),
            args.gameResult.gameSettings.minCountOfRightAnswers.toString()
        )
        binding.tvScoreAnswers.text = String.format(
            getString(R.string.score_answers),
            args.gameResult.countOfRightAnswers.toString()
        )
        binding.tvRequiredPercentage.text = String.format(
            getString(R.string.required_percentage),
            args.gameResult.gameSettings.minPercentOfRightAnswers.toString()
        )
        binding.tvScorePercentage.text = String.format(
            getString(R.string.score_percentage),
            getPercentOfRightAnswer().toString()
        )
    }

    private fun getSmileResId(): Int{
        return if (args.gameResult.winner){
            R.drawable.ic_smile
        } else {
            R.drawable.ic_sad
        }
    }

    private fun getPercentOfRightAnswer(): Int {
        if (args.gameResult.countOfQuestion == 0){
            return 0
        }
        return (args.gameResult.countOfRightAnswers / args.gameResult.countOfQuestion.toDouble() * 100).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun retryGame(){
        findNavController().popBackStack()
    }
}
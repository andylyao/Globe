package com.andy.globe

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_user.*

/**
 * A simple [Fragment] subclass.
 */
class UserFragment : DialogFragment() {


    private var userBean: UserBean? = null
    private var smallCircleTranslation: Float? = null
    private var largePhotoTranslation: Float? = null
    private var animatorSet: AnimatorSet? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.userBean = arguments?.getSerializable("userBean") as UserBean
        smallCircleTranslation = DisplayUtils.dp2px(activity, 105f).toFloat()
        largePhotoTranslation = DisplayUtils.dp2px(activity, 56f).toFloat()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        iv_large_photo.setImageResource(if(userBean?.sex == "F") R.drawable.icon_playground_user_woman else R.drawable.icon_playground_user_man)
        background_view.bindUserData(userBean)
        Glide.with(this)
            .load(userBean?.shortcut)
            .placeholder(if(userBean?.sex == "F") R.drawable.icon_playground_user_woman else R.drawable.icon_playground_user_man)
            .apply(RequestOptions().transform(CenterCrop()))
            .into(iv_photo)
        iv_photo.bindUserData(userBean)
        iv_sex.setImageResource(if (userBean?.sex == "F") R.drawable.icon_playground_user_sex_woman else R.drawable.icon_playground_user_sex_man)
        user_container.setOnClickListener {
            dismiss()
        }
        doAnimation()
    }

    private fun doAnimation() {
        animatorSet = AnimatorSet()
        val animator1 = ValueAnimator.ofFloat(0f, 1f)
        animator1.duration = 500
        animator1.interpolator = AccelerateInterpolator()
        animator1.addUpdateListener {
            rl_playground.scaleX = it.animatedValue as Float
            rl_playground.scaleY = it.animatedValue as Float
        }
        val animator2 = ValueAnimator.ofInt(0, 1)
        animator2.duration = 1000
        val animator3 = ValueAnimator.ofInt(0, 500)
        animator3.duration = 500
        animator3.interpolator = AccelerateInterpolator()
        animator3.addUpdateListener { animation ->
            if (!isVisible) return@addUpdateListener
            val scale = animation.animatedValue as Int / 500f
            ll_similar.translationX = -scale * smallCircleTranslation!!
            ll_active.translationX = scale * smallCircleTranslation!!
            val photoScale = (1 - scale) * 0.62f + 0.38f
            iv_large_photo.scaleX = photoScale
            iv_large_photo.scaleY = photoScale
            iv_large_photo.alpha = (1 - scale)
            iv_large_photo.translationY = -scale * largePhotoTranslation!!
        }
        animatorSet?.playSequentially(animator1, animator2, animator3)
        animatorSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                if (!isVisible) return
                ll_similar.translationX = -smallCircleTranslation!!
                ll_active.translationX = smallCircleTranslation!!
                iv_large_photo.alpha = 0f
                iv_large_photo.translationY = -largePhotoTranslation!!

            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorSet?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        animatorSet?.cancel()
    }
}

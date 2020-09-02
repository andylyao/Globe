package com.andy.globe

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import com.andy.globe.bean.UserBean
import com.andy.globe.util.DisplayUtils
import com.andy.globe.util.StatusBarHelper
import com.andy.globe.widget.GlobeUserView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_globe.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class GlobeFragment : Fragment() {

    private val userIcons = arrayListOf<String>(
        "https://goss.veer.com/creative/vcg/veer/800water/veer-154505400.jpg",
        "https://goss.veer.com/creative/vcg/veer/800water/veer-303033052.jpg",
        "https://pic4.zhimg.com/v2-3be05963f5f3753a8cb75b6692154d4a_1440w.jpg?source=172ae18b",
        "https://w.wallhaven.cc/full/dg/wallhaven-dg7y23.jpg",
        "https://w.wallhaven.cc/full/5w/wallhaven-5we787.jpg",
        "https://w.wallhaven.cc/full/ox/wallhaven-oxv6gl.png",
        "https://w.wallhaven.cc/full/zm/wallhaven-zmm7mw.png",
        "https://cdn.pixabay.com/photo/2020/08/26/15/41/wedding-5519806__340.jpg",
        "https://cdn.pixabay.com/photo/2020/08/21/08/46/african-5505598_960_720.jpg",
        "https://cdn.pixabay.com/photo/2015/07/09/00/29/woman-837156_960_720.jpg",
        "https://cdn.pixabay.com/photo/2020/08/23/03/41/leaves-5509797__340.jpg",
        "https://cdn.pixabay.com/photo/2020/07/25/06/41/mountains-5435903__340.jpg"
    )
    private val playgroundUsers: ArrayList<UserBean> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = inflater.inflate(R.layout.fragment_globe, null)
        return contentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutParams: ViewGroup.LayoutParams = view_placeholder.layoutParams
        layoutParams.height = StatusBarHelper.getStatusbarHeight(activity)
        view_placeholder.requestLayout()
        iv_back.setOnClickListener {
            gv_user.removeClickEvent()
            activity?.finish()
        }
        gv_user.setUserClickConsumer(Consumer {
            val dialogFragment = UserFragment()
            val arguments = Bundle()
            arguments.putSerializable("userBean", it)
            dialogFragment.arguments = arguments
            val transaction = childFragmentManager.beginTransaction()
            dialogFragment.show(transaction, "PlaygroundUser")
        })

        //check permission
        val rxPermissions = RxPermissions(activity!!)
        rxPermissions.request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe {
            if(it) {
                initGlobeUsers()
            }
        }
    }

    private fun initGlobeUsers() {
        val userList = arrayListOf<UserBean>()
        for (i in 0 .. 100) {
            val shortcut = userIcons[i % userIcons.size]
            val sex = if(i % 2 == 0) "M" else "F"
            val showRadio = Math.random()
            val dotAngle = (Math.random() * 360).toFloat()
            val userBean = UserBean("id_${i}", shortcut, sex, showRadio, dotAngle)
            userList.add(userBean)
        }
        playgroundUsers.clear()
        if(!userList.isNullOrEmpty()) {
            playgroundUsers.addAll(userList)
        }
        updatePlaygroundView(arrayListOf())
    }

    private fun updatePlaygroundView(userList: List<UserBean>) {
        gv_user.removeAllViews()
        gv_user.requestLayout()
        val showUserList = arrayListOf<UserBean>()
        val userSize = if(userList.isNullOrEmpty()) 0 else userList.size
        for (position in 0 until 100) {
            val user = (if(position < userSize) {
                userList[position]
            } else {
                val playgroundPosition = position - userSize
                getPlaygroundUser(playgroundPosition)?: break
            })
            user.showRatio = Math.random()
            user.dotAngle = (Math.random() * 360).toFloat()
            showUserList.add(user)
        }
        var count = 0
        var position = showUserList.size / 2
        while(count < showUserList.size) {
            if(position >= showUserList.size) position = 0
            val user = showUserList[position]
            val userView = GlobeUserView(activity!!, user, userClickListener)
            Glide.with(activity!!)
                .load(user.shortcut)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(DisplayUtils.dp2px(context, 40f))))
                .into(userView)
            gv_user.addView(userView)
            count ++
            position ++
        }
    }

    private fun getPlaygroundUser(position: Int): UserBean? {
        if(playgroundUsers.isNullOrEmpty()) return null
        val playgroundSize = playgroundUsers.size
        return if(position < playgroundSize) {
            playgroundUsers[position]
        } else {
            val playgroundPosition = position - playgroundSize
            getPlaygroundUser(playgroundPosition)
        }
    }

    override fun onResume() {
        super.onResume()
        gv_user.removeClickEvent()
    }

    private val userClickListener = View.OnClickListener {
        gv_user.removeClickEvent()
        gv_user.scrollToUserCenter(it as GlobeUserView)
    }
}

package com.andy.globe.bean

import java.io.Serializable

data class UserBean (
        val userId: String,
        val shortcut: String,
        val sex: String, 	//性别：M、F
        var showRatio: Double,
        var dotAngle: Float
) : Serializable
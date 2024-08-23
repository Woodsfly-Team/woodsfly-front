package com.example.woodsfly_skip.fragment
import android.os.Parcel
import android.os.Parcelable

data class Bird(
    val name: String?,
    val incidence: Double,
    val imageResourceId: Int, // 假设 image 是一个资源 ID
    val define: String?,
    val habitat: String?,
    val introduction: String?,
    val level: String?,
    val link: String?
) : Parcelable {

    // 必须的Parcelable构造函数
    constructor(parcel: Parcel) : this(
        parcel.readString(), // name
        parcel.readDouble(), // incidence
        parcel.readInt(), // imageResourceId
        parcel.readString(), // define
        parcel.readString(), // habitat
        parcel.readString(), // introduction
        parcel.readString(), // level
        parcel.readString()  // link
    ) {
    }

    // 写入Parcel的方法
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeString(name)
            writeDouble(incidence)
            writeInt(imageResourceId)
            writeString(define)
            writeString(habitat)
            writeString(introduction)
            writeString(level)
            writeString(link)
        }
    }

    // 描述内容的方法
    override fun describeContents(): Int {
        return 0
    }

    // 必须的Parcelable.Creator静态内部类
    companion object CREATOR : Parcelable.Creator<Bird> {
        override fun createFromParcel(parcel: Parcel): Bird {
            return Bird(parcel)
        }

        override fun newArray(size: Int): Array<Bird?> {
            return arrayOfNulls(size)
        }
    }
}
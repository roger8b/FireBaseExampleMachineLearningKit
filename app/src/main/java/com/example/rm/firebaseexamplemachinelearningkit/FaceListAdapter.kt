package com.example.rm.firebaseexamplemachinelearningkit

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.adapter_face_list_item.view.*


class FaceListAdapter(private val faces: MutableList<FirebaseVisionFace>, private val context: Context) : RecyclerView.Adapter<FaceListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_face_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return faces.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val face = faces[position]

        holder.headEulerAngleY.text = face.headEulerAngleY.toString()
        holder.headEulerAngleZ.text = face.headEulerAngleZ.toString()
        holder.leftEyeOpenProbability.text = face.leftEyeOpenProbability.toString()
        holder.rightEyeOpenProbability.text = face.rightEyeOpenProbability.toString()
        holder.smilingProbability.text = face.smilingProbability.toString()
        holder.trackingId.text = face.trackingId.toString()
        holder.boundingBox.text = face.boundingBox.flattenToString()
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val boundingBox = view.tv_boundingBox
        val headEulerAngleY = view.tv_headEulerAngleY
        val headEulerAngleZ = view.tv_headEulerAngleY
        val leftEyeOpenProbability = view.tv_leftEyeOpenProbability
        val rightEyeOpenProbability = view.tv_rightEyeOpenProbability
        val smilingProbability = view.tv_smilingProbability
        val trackingId = view.tv_trackingId
        val getLandmark = view.tv_getLandmark

    }
}
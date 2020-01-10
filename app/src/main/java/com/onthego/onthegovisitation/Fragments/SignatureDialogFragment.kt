package com.onthego.onthegovisitation

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.crashlytics.android.Crashlytics
import java.io.File

class SignatureDialogFragment : androidx.fragment.app.DialogFragment()
{
    lateinit var mContent : LinearLayout
    lateinit var saveSignButton : Button
    lateinit var cancelSignButton : Button
    lateinit var clearSignButton : ImageView
    lateinit var DIRECTORY : String
    lateinit var storedPath : String
    lateinit var onInputListener : OnInputListener
    lateinit var file : File
    var signatureView : View? = null

    interface OnInputListener
    {
        fun sendInput(path : String)
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        //to pass data to caller
        try
        {
            onInputListener = activity as OnInputListener
        }
        catch (e : ClassCastException)
        {
            Log.e("Signature", "onAttach: ClassCastException: " + e.message)
            Crashlytics.log(e.toString())
        }

        DIRECTORY = activity !!.applicationInfo.dataDir + "//files//Signs/"
        storedPath = DIRECTORY + "signature.png"
        file = File(DIRECTORY)
        if (! file.exists())
        {
            file.mkdir()
        }
    }

    override fun onCreateView(
            inflater : LayoutInflater,
            container : ViewGroup?,
            savedInstanceState : Bundle?
    ) : View?
    {
        try
        {
            if (signatureView != null)
            {
                val parent = signatureView !!.parent as ViewGroup
                parent.removeView(signatureView)
            }
            signatureView = inflater.inflate(R.layout.fragment_signature_dialog, container, true)

            mContent = signatureView !!.findViewById(R.id.signatureLayout)
            saveSignButton = signatureView !!.findViewById(R.id.saveButton)
            cancelSignButton = signatureView !!.findViewById(R.id.cancelButton)
            clearSignButton = signatureView !!.findViewById(R.id.clearButton)
            val mSignature = Signature.getInstance(context !!, mContent)
            mSignature !!.setBackgroundColor(Color.WHITE)
            mContent.addView(
                    mSignature,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
            val view = mContent

            saveSignButton.setOnClickListener {
                if (mSignature.lastTouchX == 0.toFloat()) return@setOnClickListener
//                view.isDrawingCacheEnabled = true
                mSignature.save(view, storedPath)
                mContent.removeAllViews()
                mSignature.clear()
                mSignature.lastTouchX = 0.toFloat()
                mSignature.lastTouchY = 0.toFloat()
                onInputListener.sendInput(storedPath) // to send saved image to caller
                dialog?.dismiss()
            }

            cancelSignButton.setOnClickListener {
                mSignature.clear()
                mContent.removeAllViews()
                dialog?.dismiss()
            }

            clearSignButton.setOnClickListener {
                mSignature.clear()
            }
        }
        catch (e : Exception)
        {
            return null
        }
        return signatureView
    }

    companion object
    {
        @JvmStatic
        fun newInstance() : SignatureDialogFragment
        {
            return SignatureDialogFragment()
        }
    }
}

package com.onthego.onthegovisitation

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.onthego.onthegovisitation.Common.BaseActivity
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ChangePasswordActivity : BaseActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        changePasswordButton.setOnClickListener()
        {
            val savedPasswordText =
                    Utils.getPreference(this@ChangePasswordActivity, GeneralClass.userPassword)
            val oldPasswordText = oldPasswordEditText.text.toString()
            val newPasswordText = newPasswordEditText.text.toString()
            val newSecondPasswordText = newPasswordSecondEditText.text.toString()

            when (attemptChangePassword())
            {
                savedPasswordText != oldPasswordText ->
                {
                    oldPasswordTextLayout.error = "Incorrect Password!"
                    return@setOnClickListener
                }
                newPasswordText != newSecondPasswordText ->
                {
                    newPasswordSecondTextLayout.error = "New password does not match!"
                    return@setOnClickListener
                }
                savedPasswordText == oldPasswordText && newPasswordText == newSecondPasswordText ->
                {
                    Utils.savePreference(
                            this@ChangePasswordActivity,
                            GeneralClass.userPassword,
                            newPasswordText
                    )
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.success))
                    builder.setMessage(getString(R.string.change_password_successfully))
                    builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    builder.create()
                    builder.show()
                }
            }
        }

        Utils.layoutEmptyTextListener(oldPasswordEditText, oldPasswordTextLayout)
        Utils.layoutEmptyTextListener(newPasswordEditText, newPasswordTextLayout)
        Utils.layoutEmptyTextListener(newPasswordSecondEditText, newPasswordSecondTextLayout)

        changePasswordLayout.setOnClickListener()
        {
            Utils.hideKeyboard(changePasswordLayout, this)
        }
    }

    private fun attemptChangePassword() : Boolean
    {
        // Reset errors.
        oldPasswordEditText.error = null
        newPasswordEditText.error = null
        newPasswordSecondEditText.error = null
        // Store values at the time of the login attempt.
        val oldPassword = oldPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val newSecondPassword = newPasswordSecondEditText.text.toString()
        var cancel = false
        var focusView : View? = null
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(oldPassword))
        {
            oldPasswordTextLayout.error = getString(R.string.error_field_required)
            focusView = oldPasswordEditText
            cancel = true
        }

        if (TextUtils.isEmpty(newPassword))
        {
            newPasswordTextLayout.error = getString(R.string.error_field_required)
            focusView = newPasswordEditText
            cancel = true
        }

        if (TextUtils.isEmpty(newSecondPassword))
        {
            newPasswordSecondTextLayout.error = getString(R.string.error_field_required)
            focusView = newPasswordSecondEditText
            cancel = true
        }

        if (cancel)
        {
            focusView?.requestFocus()
            return false
        }

        return true
    }
}
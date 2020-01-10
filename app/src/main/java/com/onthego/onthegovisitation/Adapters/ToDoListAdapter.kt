package com.onthego.onthegovisitation

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onthego.onthegovisitation.GeneralClass.*
import com.onthego.onthegovisitation.Models.ToDoMessage

class ToDoListAdapter(toDoLists: List<ToDoMessage>) :
    RecyclerView.Adapter<ToDoListAdapter.ToDoListViewHolder>() {
    private val todoList = toDoLists
    private var listener: OnItemClickListener? = null

    inner class ToDoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    listener?.onItemClick(todoList[position])
            }
        }

        private val assignDateTime = itemView.findViewById<TextView>(R.id.assignDateTimeTextView)!!
        private val assignBy = itemView.findViewById<TextView>(R.id.assignByTextView)!!
        private val toDoMessage = itemView.findViewById<TextView>(R.id.toDoMessageTextView)!!
        private var doneStatus = itemView.findViewById<ImageView>(R.id.statusImageView)!!
        private var doneStatusText = itemView.findViewById<TextView>(R.id.statusTextView)!!
        private var doneStatusColor = itemView.findViewById<ImageView>(R.id.statusColor)!!
        fun bindMessage(todoMsg: ToDoMessage) {
            assignDateTime.text = todoMsg.AssignDateTime.substring(0, 10)
            assignBy.text = todoMsg.AssignByUserID
            toDoMessage.text = todoMsg.ToDoMsg

            when {
                todoMsg.ToDoStatus == TODOStatus.None.status -> {
                    doneStatus.visibility = View.GONE
                    assignDateTime.typeface =
                        Typeface.create(assignDateTime.typeface, Typeface.BOLD)
                    assignBy.typeface = Typeface.create(assignBy.typeface, Typeface.BOLD)
                    toDoMessage.typeface = Typeface.create(toDoMessage.typeface, Typeface.BOLD)
                }
                todoMsg.ToDoStatus == TODOStatus.Read.status -> {
                    doneStatus.visibility = View.VISIBLE
                    doneStatusText.text = TODOStatusText.Read.text
                    doneStatusColor.setBackgroundColor(TODOStatusColor.Read.color)
                }
                todoMsg.ToDoStatus == TODOStatus.Complete.status -> {
                    doneStatus.visibility = View.VISIBLE
                    doneStatus.setImageResource(TODOStatusImage.Complete.image)
                    doneStatusText.text = TODOStatusText.Complete.text
                    doneStatusColor.setBackgroundColor(TODOStatusColor.Complete.color)
                }
                todoMsg.ToDoStatus == TODOStatus.FollowUp.status -> {
                    doneStatus.visibility = View.VISIBLE
                    doneStatus.setImageResource(TODOStatusImage.FollowUp.image)
                    doneStatusText.text = TODOStatusText.FollowUp.text
                    doneStatusColor.setBackgroundColor(TODOStatusColor.FollowUp.color)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ToDoListViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.todo_list, parent, false)
        return ToDoListViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(holder: ToDoListViewHolder, position: Int) {
        holder.bindMessage(todoList[position])
    }

    interface OnItemClickListener {
        fun onItemClick(todoMsg: ToDoMessage)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}

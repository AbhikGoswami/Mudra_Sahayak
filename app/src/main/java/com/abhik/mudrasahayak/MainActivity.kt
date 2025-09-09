//package com.abhik.mudrasahayak
//
//import android.Manifest
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.provider.Telephony
//import android.speech.RecognizerIntent
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.Menu
//import android.view.MenuItem
//import android.widget.EditText
//import android.widget.ImageButton
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.auth.FirebaseAuth
//import java.util.Locale
//import java.util.regex.Pattern
//
////region ------ Data Classes ------
//data class Message(val text: String, val author: Author)
//enum class Author { USER, BOT }
////endregion
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var chatRecyclerView: RecyclerView
//    private lateinit var messageEditText: EditText
//    private lateinit var voiceSendButton: ImageButton
//    private lateinit var chatAdapter: ChatAdapter
//    private val messages = mutableListOf<Message>()
//    private lateinit var auth: FirebaseAuth
//
//    private var conversationState = ConversationState.AWAITING_LOAN_AMOUNT
//    private var loanAmount = 0
//    private var panNumber = ""
//
//    // --- Launcher for Speech-to-Text ---
//    private val speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK && result.data != null) {
//            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//            if (!spokenText.isNullOrEmpty()) {
//                messageEditText.setText(spokenText[0])
//                messageEditText.setSelection(messageEditText.text.length)
//            }
//        }
//    }
//
//    private val requestSmsPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
//            if (isGranted) {
//                addBotMessage(getString(R.string.bot_sms_permission_granted))
//                analyzeSms()
//            } else {
//                addBotMessage(getString(R.string.bot_sms_permission_denied))
//            }
//        }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        auth = FirebaseAuth.getInstance()
//        chatRecyclerView = findViewById(R.id.chatRecyclerView)
//        messageEditText = findViewById(R.id.messageEditText)
//        voiceSendButton = findViewById(R.id.voiceSendButton)
//
//        chatAdapter = ChatAdapter(messages)
//        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
//            stackFromEnd = true
//        }
//        chatRecyclerView.adapter = chatAdapter
//
//        // --- Dynamic Button Logic ---
//        voiceSendButton.setOnClickListener {
//            val text = messageEditText.text.toString().trim()
//            if (text.isNotEmpty()) {
//                handleUserInput()
//            } else {
//                startVoiceInput()
//            }
//        }
//
//        // --- TextWatcher to change icon ---
//        messageEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s.isNullOrEmpty()) {
//                    voiceSendButton.setImageResource(R.drawable.ic_mic)
//                } else {
//                    voiceSendButton.setImageResource(android.R.drawable.ic_menu_send)
//                }
//            }
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        startConversation()
//    }
//
//    // --- Function to launch speech recognizer ---
//    private fun startVoiceInput() {
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
//        }
//        try {
//            speechRecognizerLauncher.launch(intent)
//        } catch (e: Exception) {
//            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menuforlogout, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_logout -> {
//                auth.signOut()
//                val intent = Intent(this, AuthActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(intent)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    private fun startConversation() {
//        if (messages.isEmpty()) {
//            addBotMessage(getString(R.string.bot_greeting))
//        }
//    }
//
//    private fun handleUserInput() {
//        val userInput = messageEditText.text.toString().trim()
//        if (userInput.isNotEmpty()) {
//            addUserMessage(userInput)
//            processConversation(userInput)
//            messageEditText.text.clear()
//        }
//    }
//
//    private fun processConversation(input: String) {
//        Handler(Looper.getMainLooper()).postDelayed({
//            when (conversationState) {
//                ConversationState.AWAITING_LOAN_AMOUNT -> {
//                    val amount = input.filter { it.isDigit() }.toIntOrNull()
//                    if (amount != null && amount > 0) {
//                        loanAmount = amount
//                        conversationState = ConversationState.AWAITING_PAN
//                        addBotMessage(getString(R.string.bot_ask_pan))
//                    } else {
//                        addBotMessage(getString(R.string.bot_invalid_amount))
//                    }
//                }
//                ConversationState.AWAITING_PAN -> {
//                    val panPattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}")
//                    if (panPattern.matcher(input.uppercase()).matches()) {
//                        panNumber = input.uppercase()
//                        conversationState = ConversationState.AWAITING_SMS_PERMISSION
//                        addBotMessage(getString(R.string.bot_processing_pan, panNumber))
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            addBotMessage(getString(R.string.bot_ask_sms_permission))
//                        }, 1500)
//                    } else {
//                        addBotMessage(getString(R.string.bot_invalid_pan))
//                    }
//                }
//                ConversationState.AWAITING_SMS_PERMISSION -> {
//                    if (input.equals(getString(R.string.user_reply_yes), ignoreCase = true) || input.equals("yes", ignoreCase = true)) {
//                        checkAndRequestSmsPermission()
//                    } else {
//                        addBotMessage(getString(R.string.bot_sms_permission_denied))
//                    }
//                }
//            }
//        }, 1000)
//    }
//
//    private fun checkAndRequestSmsPermission() {
//        when {
//            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED -> {
//                addBotMessage(getString(R.string.bot_sms_permission_granted))
//                analyzeSms()
//            }
//            else -> {
//                requestSmsPermissionLauncher.launch(Manifest.permission.READ_SMS)
//            }
//        }
//    }
//
//    private fun analyzeSms() {
//        var creditCount = 0
//        var debitCount = 0
//        var loanEmiCount = 0
//
//        try {
//            val cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null)
//            cursor?.use {
//                val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
//                if (bodyIndex != -1) {
//                    while (it.moveToNext()) {
//                        val body = it.getString(bodyIndex).lowercase(Locale.ROOT)
//                        if (body.contains("credited") || body.contains("deposited")) creditCount++
//                        if (body.contains("debited") || body.contains("withdrawn")) debitCount++
//                        if (body.contains("emi")) loanEmiCount++
//                    }
//                }
//            }
//        } catch (e: SecurityException) {
//            Toast.makeText(this, "Permission to read SMS was denied.", Toast.LENGTH_SHORT).show()
//            addBotMessage(getString(R.string.bot_sms_permission_denied))
//            return
//        }
//
//        val financialScore = (creditCount * 2) - (debitCount * 0.5) - (loanEmiCount * 3)
//        val approved = financialScore > 5 && loanAmount < 50000 // Example logic
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this, LoanOfferActivity::class.java).apply {
//                putExtra("LOAN_APPROVED", approved)
//                putExtra("LOAN_AMOUNT", loanAmount)
//            }
//            startActivity(intent)
//        }, 3000)
//    }
//
//    private fun addUserMessage(text: String) {
//        messages.add(Message(text, Author.USER))
//        chatAdapter.notifyItemInserted(messages.size - 1)
//        chatRecyclerView.scrollToPosition(messages.size - 1)
//    }
//
//    private fun addBotMessage(text: String) {
//        messages.add(Message(text, Author.BOT))
//        chatAdapter.notifyItemInserted(messages.size - 1)
//        chatRecyclerView.scrollToPosition(messages.size - 1)
//    }
//
//    enum class ConversationState {
//        AWAITING_LOAN_AMOUNT,
//        AWAITING_PAN,
//        AWAITING_SMS_PERMISSION,
//    }
//}
//



package com.abhik.mudrasahayak

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import java.util.regex.Pattern

//region ------ Data Classes ------
data class Message(val text: String, val author: Author)
enum class Author { USER, BOT }
//endregion

class MainActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var voiceSendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var auth: FirebaseAuth

    private var conversationState = ConversationState.AWAITING_LOAN_AMOUNT
    private var loanAmount = 0
    private var panNumber = ""

    // --- MODIFIED: Launcher for Speech-to-Text now automatically sends the message ---
    private val speechRecognizerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText: ArrayList<String>? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenText.isNullOrEmpty()) {
                val recognizedText = spokenText[0]
                // NEW: Automatically add the user's message to the chat
                addUserMessage(recognizedText)
                // NEW: Automatically process the conversation with the new message
                processConversation(recognizedText)
            }
        }
    }

    private val requestSmsPermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                addBotMessage(getString(R.string.bot_sms_permission_granted))
                analyzeSms()
            } else {
                addBotMessage(getString(R.string.bot_sms_permission_denied))
            }
        }

    private val requestAudioPermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchSpeechRecognizer()
            } else {
                Toast.makeText(this, "Microphone permission is required for voice input.", Toast.LENGTH_SHORT).show()
                addBotMessage("To use your voice, I need microphone access.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        voiceSendButton = findViewById(R.id.voiceSendButton)

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter

        voiceSendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                handleUserInput()
            } else {
                startVoiceInput()
            }
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    voiceSendButton.setImageResource(R.drawable.ic_mic)
                } else {
                    voiceSendButton.setImageResource(android.R.drawable.ic_menu_send)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        startConversation()
    }

    private fun startVoiceInput() {
        showPermissionPrimerDialog(
            title = getString(R.string.permission_mic_title),
            message = getString(R.string.permission_mic_message),
            positiveButtonText = getString(R.string.permission_button_ok)
        ) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                    launchSpeechRecognizer()
                }
                else -> {
                    requestAudioPermissionResultLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speak_now))
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuforlogout, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startConversation() {
        if (messages.isEmpty()) {
            addBotMessage(getString(R.string.bot_greeting))
        }
    }

    private fun handleUserInput() {
        val userInput = messageEditText.text.toString().trim()
        if (userInput.isNotEmpty()) {
            addUserMessage(userInput)
            processConversation(userInput)
            messageEditText.text.clear()
        }
    }

    private fun processConversation(input: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            when (conversationState) {
                ConversationState.AWAITING_LOAN_AMOUNT -> {
                    val amount = input.filter { it.isDigit() }.toIntOrNull()
                    if (amount != null && amount > 0) {
                        loanAmount = amount
                        conversationState = ConversationState.AWAITING_PAN
                        addBotMessage(getString(R.string.bot_ask_pan))
                    } else {
                        addBotMessage(getString(R.string.bot_invalid_amount))
                    }
                }
                ConversationState.AWAITING_PAN -> {
                    val panPattern = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]{1}")
                    if (panPattern.matcher(input.uppercase()).matches()) {
                        panNumber = input.uppercase()
                        conversationState = ConversationState.AWAITING_SMS_PERMISSION
                        addBotMessage(getString(R.string.bot_processing_pan, panNumber))
                        Handler(Looper.getMainLooper()).postDelayed({
                            addBotMessage(getString(R.string.bot_ask_sms_permission))
                        }, 1500)
                    } else {
                        addBotMessage(getString(R.string.bot_invalid_pan))
                    }
                }
                ConversationState.AWAITING_SMS_PERMISSION -> {
                    if (input.equals(getString(R.string.user_reply_yes), ignoreCase = true) || input.equals("yes", ignoreCase = true)) {
                        checkAndRequestSmsPermission()
                    } else {
                        addBotMessage(getString(R.string.bot_sms_permission_denied))
                    }
                }
            }
        }, 1000)
    }

    private fun checkAndRequestSmsPermission() {
        showPermissionPrimerDialog(
            title = getString(R.string.permission_sms_title),
            message = getString(R.string.permission_sms_message),
            positiveButtonText = getString(R.string.permission_button_ok)
        ) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED -> {
                    analyzeSms()
                }
                else -> {
                    requestSmsPermissionResultLauncher.launch(Manifest.permission.READ_SMS)
                }
            }
        }
    }

    private fun analyzeSms() {
        var creditCount = 0
        var debitCount = 0
        var loanEmiCount = 0

        try {
            val cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null)
            cursor?.use {
                val bodyIndex = it.getColumnIndex(Telephony.Sms.Inbox.BODY)
                if (bodyIndex != -1) {
                    while (it.moveToNext()) {
                        val body = it.getString(bodyIndex).lowercase(Locale.ROOT)
                        if (body.contains("credited") || body.contains("deposited")) creditCount++
                        if (body.contains("debited") || body.contains("withdrawn")) debitCount++
                        if (body.contains("emi")) loanEmiCount++
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permission to read SMS was denied.", Toast.LENGTH_SHORT).show()
            addBotMessage(getString(R.string.bot_sms_permission_denied))
            return
        }

        val financialScore = (creditCount * 2) - (debitCount * 0.5) - (loanEmiCount * 3)
        val approved = financialScore > 5 && loanAmount < 50000 // Example logic

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoanOfferActivity::class.java).apply {
                putExtra("LOAN_APPROVED", approved)
                putExtra("LOAN_AMOUNT", loanAmount)
            }
            startActivity(intent)
        }, 3000)
    }

    private fun addUserMessage(text: String) {
        messages.add(Message(text, Author.USER))
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun addBotMessage(text: String) {
        messages.add(Message(text, Author.BOT))
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun showPermissionPrimerDialog(title: String, message: String, positiveButtonText: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.permission_button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    enum class ConversationState {
        AWAITING_LOAN_AMOUNT,
        AWAITING_PAN,
        AWAITING_SMS_PERMISSION,
    }
}




// Imports and firebase references have been omitted for security purposess 

class CheckInViewModel(
    private val firebase: FirebaseDatabase = Firebase.database,
    private val _accountViewModel: AccountViewModel
) : ViewModel() {

    private val tag: String = "CheckInViewModel"
    private val reference: String = ""

    private val _checkInState = MutableStateFlow(false)
    val checkInState = _checkInState.asStateFlow()

    private val _friendCheckInState = MutableStateFlow(false)
    val friendCheckInState = _friendCheckInState.asStateFlow()

    private val _isCheckInAllowed = MutableStateFlow(false)
    val isCheckInAllowed = _isCheckInAllowed.asStateFlow()

    fun onSetCheckInEnabled(newStatus: Boolean) {
        _isCheckInAllowed.value = newStatus
    }

    fun onListenToFriendCheckInStatus(scheduleId: String?, friendId:String?) {
        if (scheduleId.isNullOrEmpty() || friendId.isNullOrEmpty()) {
            return
        }
        val reference = firebase.getReference("")
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _friendCheckInState.value = snapshot.child("checkedIn").getValue(Boolean::class.java) ?: false
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Log.i("$tag -> onListenToFriendCheckInStatus()", error.message)
            }
        })
    }

    fun onListenToCheckInStatus(userId: String?, scheduleId: String?) {
        if (!userId.isNullOrEmpty() && !scheduleId.isNullOrEmpty()) {
            val reference = firebase.getReference("")
            reference.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val isCheckedIn = snapshot.child("checkedIn").getValue(Boolean::class.java) ?: false
                        _checkInState.value = isCheckedIn
                    }
                    else {
                        _checkInState.value = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i("$tag -> onListenToCheckInStatus", error.message)
                }
            })
        }
    }

    suspend fun onDeleteCheckInStatus(uid: String, scheduleId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val reference = firebase.getReference("")
                    reference.addValueEventListener(object: ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                reference.removeValue().addOnSuccessListener {
                                    _checkInState.value = false
                                    _friendCheckInState.value = false
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.i("$tag -> onDeleteCheckInStatus()", error.message)
                        }
                    })
                }
                catch (e: Exception) {
                    Log.i(
                        "$tag -> onDeleteCheckInStatus()",
                        e.message ?: e.stackTraceToString()
                    )
                }
            }
        }
    }

    suspend fun onPostCheckInStatus(senderId:String?, scheduleId: String, recipientId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    _accountViewModel.userData.value?.userId?.let { loggedInUser ->
                        val reference = firebase.getReference("")
                        val creatorCheckInValues = CheckInDto(
                            senderId = loggedInUser,
                            scheduleId = scheduleId,
                            recipientId = recipientId,
                            isCheckedIn = true
                        )
                        reference.setValue(creatorCheckInValues).await()
                    }
                }
                catch (e: Exception) {
                    Log.i(
                        "$tag -> onPostCheckInNotification()",
                        e.message ?: e.stackTraceToString()
                    )
                }
            }
        }
    }
}

// Imports have been intentially been omitted 

@Composable
fun ScheduleBottomSheet(
    modifier: Modifier,
    sheetState: SheetState,
    scheduleEntity: ScheduleEntity?,
    isSheetOpen: Boolean,
    workoutDetails: SmallWorkoutDto,
    onDismissRequest: () -> Unit,
    onDeleteWorkout: (Boolean) -> Boolean,
    onDeleteWorkoutSeries: (String) -> Unit,
    onEditWorkout: () -> Unit,
    onMakeWorkoutSolo: () -> Unit,
    onInviteFriends: (SmallWorkoutDto) -> Unit
) {
    val accountViewModel: AccountViewModel = koinViewModel()
    val loggedInUser = accountViewModel.userData.collectAsStateWithLifecycle(initialValue = null)
    val forestGreen = Color(0xFF228B22)
    val textColour = Color.White

    if (isSheetOpen) {
        ModalBottomSheet(
            modifier = modifier,
            containerColor = DarkerBlueGray,
            contentColor = Color.White,
            sheetState = sheetState,
            onDismissRequest = { onDismissRequest() }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 16.dp, end=16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (workoutDetails.workoutPartnerName.isNotEmpty()) {
                            val statusModifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                            val iconModifier = Modifier.size(15.dp)
                            AsyncImage(
                                model = workoutDetails.profilePicUrl,
                                contentDescription = "profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(35.dp).clip(CircleShape)
                            )
                            Text(
                                text = workoutDetails.workoutPartnerName,
                                color = textColour,
                                fontSize = 12.sp
                            )

                            when (scheduleEntity?.status) {
                                "pending" -> {
                                    Text(
                                        modifier = statusModifier,
                                        text = "Pending",
                                        fontSize = 10.sp,
                                        color = textColour,
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        modifier = iconModifier,
                                        painter = painterResource(id = R.drawable.baseline_pending_24),
                                        contentDescription = "account_icon",
                                        tint = Color.LightGray
                                    )
                                }

                                "confirmed" -> {
                                    Text(
                                        modifier = statusModifier,
                                        text = "Confirmed",
                                        fontSize = 10.sp,
                                        color = textColour,
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        modifier = iconModifier,
                                        painter = painterResource(id = R.drawable.baseline_check_circle_24),
                                        contentDescription = "account_icon",
                                        tint = forestGreen
                                    )
                                }

                                "deleted" -> {
                                    Text(
                                        modifier = statusModifier,
                                        text = "Declined",
                                        fontSize = 10.sp,
                                        color = textColour,
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        modifier = iconModifier,
                                        painter = painterResource(id = R.drawable.baseline_cancel_24),
                                        contentDescription = "account_icon",
                                        tint = Color.Red
                                    )
                                }
                            }
                        } else {
                            loggedInUser.value?.let {  userdata ->
                                userdata.username?.let {  username ->
                                    AsyncImage(
                                        model = userdata.profilePictureUrl,
                                        contentDescription = "profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(35.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        modifier = Modifier.padding(end = 4.dp),
                                        text = username,
                                        fontSize = 10.sp,
                                        color = textColour,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(start = 32.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 1.dp),
                            text = workoutDetails.date,
                            fontSize = 14.sp,
                            color = textColour,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = workoutDetails.time,
                            color = textColour,
                            fontSize = 12.sp
                        )

                        Text(
                            modifier = Modifier.padding(top = 2.dp),
                            text = workoutDetails.workoutName,
                            color = textColour,
                            fontSize = 10.sp
                        )
                    }
                }

                Divider(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.White.copy(0.5f)
                )

                scheduleEntity?.let {
                    if (!it.isCreator) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                        ) {
                            InfoMessage(
                                modifier = Modifier.fillMaxWidth(),
                                description = "Only the workout creator can make changes",
                                textSize = 12.sp
                            )
                        }
                    }

                    if (it.accompanyingUserUid.isEmpty() && it.isCreator) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .clickable { onInviteFriends(workoutDetails) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(26.dp),
                                imageVector = Icons.Default.PersonAdd,
                                tint = Color.White.copy(0.5f),
                                contentDescription = "invite_friend"
                            )
                            Text(
                                modifier=Modifier.padding(start=16.dp),
                                text = "Invite friend",
                                color = textColour,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (it.isCreator) {
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .clickable { onEditWorkout() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                tint = Color.White.copy(0.5f),
                                contentDescription = "edit_workout"
                            )
                            Text(
                                modifier=Modifier.padding(start=16.dp),
                                text = "Edit workout",
                                color = textColour,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (it.isCreator && it.accompanyingUserUid.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clickable {
                                    onDeleteWorkout(false)
                                    onMakeWorkoutSolo()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(26.dp),
                                imageVector = Icons.Default.Person,
                                tint = Color.White.copy(0.5f),
                                contentDescription = "make_solo_workout"
                            )
                            Text(
                                modifier=Modifier.padding(start=16.dp),
                                text = "Change to solo workout",
                                color = textColour,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (it.reoccurringScheduleId.isNotEmpty()) {
                        // Delete workout series
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                                .clickable { onDeleteWorkoutSeries(it.reoccurringScheduleId) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircle,
                                tint = Color.White.copy(0.5f),
                                contentDescription = "remove_workout"
                            )
                            Text(
                                modifier=Modifier.padding(start=16.dp),
                                text = "Delete workout series",
                                color = textColour,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .clickable { onDeleteWorkout(true) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        tint = Color.White.copy(0.5f),
                        contentDescription = "remove_workout"
                    )
                    Text(
                        modifier=Modifier.padding(start=16.dp),
                        text = "Remove workout",
                        color = textColour,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

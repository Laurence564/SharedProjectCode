@RunWith(JUnit4::class)
class ReoccurringSchedulesVMTests {
    private val generateTestData: () -> List<ScheduleEntity> = {
        val numberOfReoccurringSchedulesForGeneration = 4
        val numberOfOneOffSchedulesForGeneration = 2
        val fullScheduleList = mutableListOf<ScheduleEntity>()
        val reoccurringScheduleIds = listOf("95c6c60f-1809-496c-98e9-60ef7a94308d", "7a9e1198-a085-4ac8-a1e0-3e85eacefc24", "93e0f792-7950-4df8-8106-12acb2a8c395", "7b955733-4d5a-484e-8a91-623298328763")
        val repeatNeverScheduleIds = listOf("d096fe1b-a4a1-4842-91e5-bb323268202e", "a5c459a4-fb21-4fdd-b35f-8d1c023d7d66")
        val groupScheduleId = "7b955733-4d5a-484e-8a91-623298328763"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 1)

        for (i in 0..< numberOfReoccurringSchedulesForGeneration) {
            val s = ScheduleEntity(
                scheduleId = reoccurringScheduleIds[i],
                reoccurringScheduleId = groupScheduleId,
                userId = "1",
                workoutId = 1L,
                accompanyingUserUid = "",
                dayDate = calendar.timeInMillis,
                time = calendar.timeInMillis,
                repeat = "Weekly"
            )
            fullScheduleList.add(s)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        for (i in 0..< numberOfOneOffSchedulesForGeneration) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val s = ScheduleEntity(
                scheduleId = repeatNeverScheduleIds[i],
                reoccurringScheduleId = "",
                userId = "1",
                workoutId = 1L,
                accompanyingUserUid = "",
                dayDate = calendar.timeInMillis,
                time = calendar.timeInMillis,
                repeat = "Never"
            )
            fullScheduleList.add(s)
        }

        fullScheduleList
    }
    private val workoutSchedules = generateTestData()
    private val reoccurringScheduleViewModel = ReoccurringWorkoutsDelegate()

    @Test
    fun getFirstWorkoutInSeries_returns_first_workout() {
        // Arrange
        val shuffledList = workoutSchedules.shuffled()

        // Act
        val actualSchedule = reoccurringScheduleViewModel.getFirstWorkoutInSeries(shuffledList, "7b955733-4d5a-484e-8a91-623298328763")

        // Assert
        val expectedScheduleId = "95c6c60f-1809-496c-98e9-60ef7a94308d"
        Assert.assertEquals(expectedScheduleId, actualSchedule?.scheduleId)
    }

    @Test
    fun getIndexPositionOfWorkoutInSeries_WhenWorkoutExistsInSchedules_ShouldReturnPositiveNumberTwo() {
        // Arrange
        val scheduleIndexUnderTest = ScheduleEntity(
            scheduleId = "93e0f792-7950-4df8-8106-12acb2a8c395", reoccurringScheduleId = "7b955733-4d5a-484e-8a91-623298328763",
            userId = "1", workoutId = 1L, accompanyingUserUid = "", dayDate = 1735035234778L, time = 1733223654778L, repeat = "Weekly"
        )

        // Act
        val actualIndexPosition = reoccurringScheduleViewModel.getIndexPositionOfWorkoutInSeries(workoutSchedules, scheduleIndexUnderTest)

        // Assert
        val expectedIndexPosition = 2
        Assert.assertEquals(expectedIndexPosition, actualIndexPosition)
    }

    @Test
    fun getIndexPositionOfWorkoutInSeries_WhenWorkoutIsNotInSeries_ShouldReturnNegativeOne() {
        // Arrange
        val scheduleIndexUnderTest = ScheduleEntity(
            scheduleId = "038749d4-f642-40c4-aa5b-094854aa0e43", reoccurringScheduleId = "", userId = "1",
            workoutId = 3L, accompanyingUserUid = "", dayDate = 1L, time = 1L, repeat = "Never"
        )

        // Act
        val actualIndexPosition = reoccurringScheduleViewModel.getIndexPositionOfWorkoutInSeries(workoutSchedules, scheduleIndexUnderTest)

        // Assert
        val expectedIndexPosition = -1
        Assert.assertEquals(expectedIndexPosition, actualIndexPosition)
    }

    @Test
    fun calculateStartingScheduleForSeries_WhenBeginningScheduleWouldBeOutOfDate_ShouldReturnFirstScheduleInSeriesWhichIsInDates() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -2)

        val minusOneHourOfCurrentTime = calendar.timeInMillis
        val scheduleInEdit = workoutSchedules[1].copy(time = minusOneHourOfCurrentTime)

        // Act
        val actualStartingSchedule = reoccurringScheduleViewModel.calculateStartingScheduleForSeries(
            upcomingSchedules = workoutSchedules,
            scheduleInEdit = scheduleInEdit,
            incomingScheduleValues = UpdateScheduleFields(
                date = scheduleInEdit.dayDate,
                time = scheduleInEdit.time,
                workoutId = 1L,
                repeat = "Weekly"
            )
        )

        // Assert
        val expectedStartingSchedule = workoutSchedules[1]
        Assert.assertEquals(expectedStartingSchedule.scheduleId, actualStartingSchedule.scheduleId)

    }

    @Test
    fun calculateStartingScheduleForSeries_WhenAllSchedulesWillBeInDate_ShouldReturnTheFirstWorkoutInTheSeries() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 1)

        val addOneHourOfCurrentTime = calendar.timeInMillis
        val scheduleInEdit = workoutSchedules[1].copy(time = addOneHourOfCurrentTime)

        // Act
        val actualStartingSchedule = reoccurringScheduleViewModel.calculateStartingScheduleForSeries(
            upcomingSchedules = workoutSchedules,
            scheduleInEdit = scheduleInEdit,
            incomingScheduleValues = UpdateScheduleFields(
                date = scheduleInEdit.dayDate,
                time = scheduleInEdit.time,
                workoutId = 1L,
                repeat = "Weekly"
            )
        )

        val actualDateTimeAsString = "${Tools.convertLongToTime(actualStartingSchedule.dayDate)} ${Tools.convertLongToTime(actualStartingSchedule.time, "HH:mm")}"
        val expectedDateTimeAsString = "${Tools.convertLongToTime(workoutSchedules[0].dayDate)} ${Tools.convertLongToTime(workoutSchedules[0].time, "HH:mm")}"

        // Assert
        Assert.assertEquals(expectedDateTimeAsString, actualDateTimeAsString)
    }

    @Test
    fun calculateStartingScheduleForSeries_WhenScheduleRepeatStatusIsNever_StartingPositionIsScheduleInEdit() {
        // Arrange
        val scheduleInEdit = workoutSchedules.firstOrNull { it.repeat.lowercase() == "never" }
        if (scheduleInEdit == null) { Assert.fail("Expected test data to contain schedule with repeat status 'Never'") }

        // Act
        val actualStartingSchedule = reoccurringScheduleViewModel.calculateStartingScheduleForSeries(
            upcomingSchedules = workoutSchedules,
            scheduleInEdit = scheduleInEdit!!,
            incomingScheduleValues = UpdateScheduleFields(
                date = 1L,
                time = 1L,
                workoutId = 1L,
                repeat = "Never"
            )
        )

        // Assert
        Assert.assertEquals(scheduleInEdit.scheduleId, actualStartingSchedule.scheduleId)
    }
}

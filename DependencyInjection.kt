// Small sample of DI with Koin

val workoutAppModule = module {

    single<WorkoutLibraryRepository> {
        Graph.workoutLibraryRepository
    }

    viewModel {
        WorkoutLibraryViewModel(
            _workoutLibraryRepository = get(),
            _accountViewModel = get()
        )
    }

    viewModel {
        WorkoutBuilderViewModel(
            _workoutLibraryRepository = get(),
            _accountViewModel = get()
        )
    }
}

class App: Application() {
  override fun onCreate() {
        super.onCreate()
        Graph.provide(this)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(workoutAppModule)
        }
    }
}

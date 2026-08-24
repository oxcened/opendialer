package dev.alenajam.opendialer.feature.inCall.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InCallCommandsModule {
    @Binds
    abstract fun bindInCallCommands(telecomAdapter: TelecomAdapter): InCallCommands
}

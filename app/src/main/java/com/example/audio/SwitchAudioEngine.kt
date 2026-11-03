package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.ui.components.ActiveGameGenre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Low-latency 16-bit 44.1kHz PCM Audio Synthesizer & Sound Engine for Eglantine.
 * Generates real-time game soundtracks and responsive controller/gameplay SFX
 * using Android's native AudioTrack API.
 */
class SwitchAudioEngine {

    private val sampleRate = 44100
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var musicJob: Job? = null

    @Volatile
    var isMuted: Boolean = false

    @Volatile
    var isPaused: Boolean = false

    // Musical note frequencies (Hz)
    private val NOTE_REST = 0.0
    private val NOTE_A3 = 220.00
    private val NOTE_B3 = 246.94
    private val NOTE_C4 = 261.63
    private val NOTE_D4 = 293.66
    private val NOTE_E4 = 329.63
    private val NOTE_F4 = 349.23
    private val NOTE_G4 = 392.00
    private val NOTE_GS4 = 415.30
    private val NOTE_A4 = 440.00
    private val NOTE_B4 = 493.88
    private val NOTE_C5 = 523.25
    private val NOTE_D5 = 587.33
    private val NOTE_E5 = 659.25
    private val NOTE_F5 = 698.46
    private val NOTE_G5 = 783.99
    private val NOTE_A5 = 880.00
    private val NOTE_C6 = 1046.50

    // Classic Korobeiniki-inspired Tetris arcade melody (Pair of frequency Hz and duration ms)
    private val tetrisMelody = listOf(
        NOTE_E5 to 360, NOTE_B4 to 180, NOTE_C5 to 180, NOTE_D5 to 360,
        NOTE_C5 to 180, NOTE_B4 to 180, NOTE_A4 to 360, NOTE_A4 to 180,
        NOTE_C5 to 180, NOTE_E5 to 360, NOTE_D5 to 180, NOTE_C5 to 180,
        NOTE_B4 to 540, NOTE_C5 to 180, NOTE_D5 to 360, NOTE_E5 to 360,
        NOTE_C5 to 360, NOTE_A4 to 360, NOTE_A4 to 360, NOTE_REST to 180,
        NOTE_D5 to 360, NOTE_F5 to 180, NOTE_A5 to 360, NOTE_G5 to 180,
        NOTE_F5 to 180, NOTE_E5 to 540, NOTE_C5 to 180, NOTE_E5 to 360,
        NOTE_D5 to 180, NOTE_C5 to 180, NOTE_B4 to 360, NOTE_B4 to 180,
        NOTE_C5 to 180, NOTE_D5 to 360, NOTE_E5 to 360, NOTE_C5 to 360,
        NOTE_A4 to 360, NOTE_A4 to 360, NOTE_REST to 180
    )

    // High-energy Kart Racing synth arpeggio
    private val racingMelody = listOf(
        NOTE_C4 to 160, NOTE_G4 to 160, NOTE_C5 to 160, NOTE_E5 to 160,
        NOTE_D5 to 160, NOTE_C5 to 160, NOTE_G4 to 160, NOTE_E4 to 160,
        NOTE_F4 to 160, NOTE_A4 to 160, NOTE_C5 to 160, NOTE_F5 to 160,
        NOTE_E5 to 160, NOTE_C5 to 160, NOTE_G4 to 160, NOTE_D5 to 240
    )

    // Atmospheric Zelda / Open-World adventure arpeggio
    private val zeldaMelody = listOf(
        NOTE_A4 to 420, NOTE_E5 to 420, NOTE_D5 to 280, NOTE_C5 to 280,
        NOTE_B4 to 420, NOTE_G4 to 420, NOTE_E4 to 560, NOTE_REST to 220,
        NOTE_F4 to 380, NOTE_C5 to 380, NOTE_B4 to 280, NOTE_G4 to 380,
        NOTE_A4 to 650, NOTE_REST to 250
    )

    // Upbeat Platformer melody
    private val platformerMelody = listOf(
        NOTE_E5 to 180, NOTE_E5 to 180, NOTE_REST to 140, NOTE_E5 to 180,
        NOTE_REST to 140, NOTE_C5 to 180, NOTE_E5 to 320, NOTE_G5 to 360,
        NOTE_REST to 300, NOTE_G4 to 360, NOTE_REST to 260,
        NOTE_C5 to 280, NOTE_G4 to 240, NOTE_E4 to 280, NOTE_A4 to 240,
        NOTE_B4 to 240, NOTE_A4 to 200, NOTE_G4 to 240, NOTE_E5 to 240,
        NOTE_G5 to 240, NOTE_A5 to 300, NOTE_F5 to 200, NOTE_G5 to 240
    )

    /**
     * Starts streaming background game music for the active genre.
     */
    fun startMusicLoop(genre: ActiveGameGenre) {
        stopMusicLoop()
        val melody = when (genre) {
            ActiveGameGenre.TETRIS -> tetrisMelody
            ActiveGameGenre.RACING -> racingMelody
            ActiveGameGenre.OPEN_WORLD_ZELDA -> zeldaMelody
            else -> platformerMelody
        }

        musicJob = engineScope.launch {
            var track: AudioTrack? = null
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                var noteIndex = 0
                var phase = 0.0
                var subPhase = 0.0

                while (isActive) {
                    if (isMuted || isPaused) {
                        val silence = ShortArray(2048)
                        track.write(silence, 0, silence.size)
                        continue
                    }

                    val (freq, durationMs) = melody[noteIndex % melody.size]
                    noteIndex++

                    val totalSamples = (sampleRate * durationMs) / 1000
                    val pcm = ShortArray(totalSamples)
                    val attackSamples = (sampleRate * 0.012).toInt().coerceAtLeast(1)
                    val releaseSamples = (sampleRate * 0.045).toInt().coerceAtLeast(1)

                    val phaseStep = if (freq > 0.0) (2.0 * PI * freq) / sampleRate else 0.0
                    val subStep = phaseStep * 0.5 // Octave sub-harmonic for warm retro fullness

                    for (i in 0 until totalSamples) {
                        if (freq <= 0.0 || isMuted || isPaused) {
                            pcm[i] = 0
                        } else {
                            // Envelope to prevent clicking and give musical pluck
                            val env = when {
                                i < attackSamples -> i.toDouble() / attackSamples
                                i > totalSamples - releaseSamples -> ((totalSamples - i).toDouble() / releaseSamples).coerceAtLeast(0.0)
                                else -> 0.85
                            }
                            // Rich harmonic blend (fundamental + soft square 3rd harmonic + sub-bass)
                            val fundamental = sin(phase)
                            val thirdHarmonic = sin(phase * 3.0) * 0.25
                            val subBass = sin(subPhase) * 0.30
                            val sampleVal = ((fundamental + thirdHarmonic + subBass) * 0.55 * env * 6500.0)
                            pcm[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                            phase += phaseStep
                            subPhase += subStep
                        }
                    }

                    track.write(pcm, 0, pcm.size)
                }
            } catch (_: Exception) {
                // Ignore audio hardware exceptions on headless/test devices
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopMusicLoop() {
        val job = musicJob
        musicJob = null
        if (job != null) {
            engineScope.launch {
                try {
                    job.cancelAndJoin()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Plays the two-tone Nintendo Switch boot snap sound effect.
     */
    fun playBootChime() {
        if (isMuted) return
        playToneSequence(
            listOf(
                ToneSegment(startHz = 987.77, endHz = 987.77, durationMs = 70, volume = 0.35),
                ToneSegment(startHz = 1318.51, endHz = 1318.51, durationMs = 150, volume = 0.42)
            )
        )
    }

    /**
     * Plays a crisp movement tick when moving pieces/characters with D-Pad or Stick.
     */
    fun playMoveTick() {
        if (isMuted || isPaused) return
        playToneSequence(
            listOf(
                ToneSegment(startHz = 540.0, endHz = 620.0, durationMs = 32, volume = 0.28)
            )
        )
    }

    /**
     * Plays a rising pitch sweep for rotation or jumping (A / X / Up).
     */
    fun playRotateOrJump() {
        if (isMuted || isPaused) return
        playToneSequence(
            listOf(
                ToneSegment(startHz = 440.0, endHz = 880.0, durationMs = 85, volume = 0.35)
            )
        )
    }

    /**
     * Plays a punchy impact/action sound for hard drop, boost, or sword slash (Y / B / ZL / ZR).
     */
    fun playDropOrAction() {
        if (isMuted || isPaused) return
        playToneSequence(
            listOf(
                ToneSegment(startHz = 360.0, endHz = 140.0, durationMs = 95, volume = 0.42)
            )
        )
    }

    /**
     * Plays a bright 4-note fanfare when clearing a line in Tetris or scoring points.
     */
    fun playScoreOrLineClear() {
        if (isMuted || isPaused) return
        playToneSequence(
            listOf(
                ToneSegment(startHz = NOTE_C5, endHz = NOTE_C5, durationMs = 65, volume = 0.40),
                ToneSegment(startHz = NOTE_E5, endHz = NOTE_E5, durationMs = 65, volume = 0.40),
                ToneSegment(startHz = NOTE_G5, endHz = NOTE_G5, durationMs = 65, volume = 0.42),
                ToneSegment(startHz = NOTE_C6, endHz = NOTE_C6, durationMs = 140, volume = 0.45)
            )
        )
    }

    private data class ToneSegment(
        val startHz: Double,
        val endHz: Double,
        val durationMs: Int,
        val volume: Double
    )

    private fun playToneSequence(segments: List<ToneSegment>) {
        engineScope.launch {
            var sfxTrack: AudioTrack? = null
            try {
                val totalDurationMs = segments.sumOf { it.durationMs }
                val totalSamples = ((sampleRate * totalDurationMs) / 1000).coerceAtLeast(512)
                val buffer = ShortArray(totalSamples)

                var sampleCursor = 0
                var phase = 0.0

                for (seg in segments) {
                    val segSamples = (sampleRate * seg.durationMs) / 1000
                    val fadeSamples = (segSamples * 0.18).toInt().coerceAtLeast(1)
                    for (i in 0 until segSamples) {
                        if (sampleCursor >= totalSamples) break
                        val progress = i.toDouble() / segSamples.coerceAtLeast(1)
                        val freq = seg.startHz + (seg.endHz - seg.startHz) * progress
                        val env = when {
                            i < fadeSamples -> i.toDouble() / fadeSamples
                            i > segSamples - fadeSamples -> (segSamples - i).toDouble() / fadeSamples
                            else -> 1.0
                        }
                        val wave = (sin(phase) + 0.3 * sin(phase * 2.0)) * 0.75
                        val pcmVal = (wave * env * seg.volume * 14000.0).toInt()
                        buffer[sampleCursor++] = pcmVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        phase += (2.0 * PI * freq) / sampleRate
                    }
                }

                val byteCount = totalSamples * 2
                sfxTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(byteCount.coerceAtLeast(1024))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                sfxTrack.write(buffer, 0, buffer.size)
                sfxTrack.play()
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 50L)
            } catch (_: Exception) {
                // Ignore on non-audio environments
            } finally {
                try {
                    sfxTrack?.stop()
                    sfxTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }
}

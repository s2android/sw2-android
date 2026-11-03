package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EglantineRepository
import com.example.model.AAAPreset
import com.example.model.SnapdragonChipset
import com.example.util.BoxArtScraper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Eglantine", appName)
  }

  @Test
  fun `verify snapdragon optimized vulkan drivers exist`() {
    val repo = EglantineRepository()
    val drivers = repo.drivers.value
    assertTrue("Drivers list must not be empty", drivers.isNotEmpty())
    val turnipDriver = drivers.find { it.id == "turnip_8gen3_opt" }
    assertTrue("Mesa Turnip Snapdragon driver must exist", turnipDriver != null)
    assertTrue("Mesa Turnip driver must be open source", turnipDriver?.isOpenSource == true)
    assertTrue("Must be Snapdragon optimized", turnipDriver?.snapdragonOptimized == true)
    assertTrue("Must support GMEM bypass", turnipDriver?.gmemTilingBypass == true)
    assertTrue("Must support UBWC compression", turnipDriver?.ubwcSupported == true)
  }

  @Test
  fun `verify snapdragon profile application`() {
    val repo = EglantineRepository()
    repo.applySnapdragonChipsetProfile(SnapdragonChipset.SD_8_ELITE)
    val config = repo.snapdragonConfig.value
    assertEquals(SnapdragonChipset.SD_8_ELITE, config.targetChipset)
    assertTrue(config.gmemBypassEnabled)
    assertTrue(config.ubwcCompression)
    assertTrue(config.adrenoTurboBoost)
    assertEquals("turnip_8elite_opt", repo.selectedDriverId.value)
  }

  @Test
  fun `verify demo games are removed and added games persist across restarts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo1 = EglantineRepository()
    repo1.attachContext(context)
    assertTrue("No demo games should exist by default", repo1.games.value.isEmpty())

    // Add a user ROM and verify it persists in a fresh repository instance
    val testGame = com.example.model.Game(
      title = "Tetris Forever",
      programId = "01007EF00011E000",
      developer = "Digital Eclipse",
      version = "1.0.2",
      fileExtension = "nsp",
      path = "Internal / Games / Tetris Forever.nsp",
      isNx2 = false,
      isSample = false,
      boxArtUrl = BoxArtScraper.getInstantFallbackCoverUrl("Tetris Forever", "01007EF00011E000")
    )
    repo1.addCustomGame(testGame)
    assertEquals(1, repo1.games.value.size)
    assertNotNull("Box art URL should be resolved", repo1.games.value.first().boxArtUrl)

    val repo2 = EglantineRepository()
    repo2.attachContext(context)
    assertEquals("Persisted game must be restored on restart", 1, repo2.games.value.size)
    assertEquals("Tetris Forever", repo2.games.value.first().title)
  }

  @Test
  fun `verify AAA preset application for Zelda TOTK and Fire Emblem Build 104`() {
    val repo = EglantineRepository()
    repo.applyAAAPreset(AAAPreset.TOTK)
    val settings = repo.settings.value
    assertEquals("Native Code Execution (NCE)", settings.cpuBackend)
    assertTrue("Fastmem must be enabled for TOTK", settings.fastmemEnabled)
    assertTrue("6GB Extended DRAM must be enabled for TOTK", settings.extendedDramMode)
    assertTrue("Async shader compilation must be enabled", settings.asyncShaderCompilation)
    assertTrue("Reactive flushing must be enabled for Zelda bloom fix", settings.reactiveFlushing)
    assertEquals("BC7 (Fast)", settings.astcRecompression)
    assertEquals("turnip_8gen3_opt", repo.selectedDriverId.value)

    // Test upstream zortos293/Eglantine Build 104 Fire Emblem preset
    repo.applyAAAPreset(AAAPreset.FIRE_EMBLEM_FW)
    val feSettings = repo.settings.value
    assertTrue("FE spatial upscale stand-in must be enabled", feSettings.feSpatialUpscaleStandin)
    assertTrue("NX2 GSV sparse tile pre-free must be enabled", feSettings.nx2GsvSparsePreFree)
    assertTrue("ARM64 reservation pre-check must be enabled", feSettings.arm64ReservationPreCheck)
  }

  @Test
  fun `verify AAA preset application for Super Mario Odyssey`() {
    val repo = EglantineRepository()
    repo.applyAAAPreset(AAAPreset.ODYSSEY)
    val settings = repo.settings.value
    assertEquals("Native Code Execution (NCE)", settings.cpuBackend)
    assertTrue("Fastmem must be enabled", settings.fastmemEnabled)
    assertTrue("Docked mode enabled for 1080p Mario", settings.isDockedMode)
    assertEquals("Mailbox", settings.vsyncMode)
  }

  @Test
  fun `verify APK update info and download progress`() {
    val repo = EglantineRepository()
    val updateInfo = repo.updateInfo.value
    assertEquals("v1.2.0", updateInfo.currentVersion)
    assertTrue("New version available", updateInfo.latestVersion.contains("v1.3.0"))
    assertEquals(18.9f, updateInfo.apkFileSizeMb, 0.01f)
    assertEquals(com.example.model.ApkUpdateStatus.UPDATE_AVAILABLE, updateInfo.status)

    // Test download progress transition
    repo.updateApkDownloadState(
      status = com.example.model.ApkUpdateStatus.DOWNLOADING,
      progress = 0.5f,
      downloadedMb = 21.4f,
      speed = "14.5 MB/s"
    )
    val downloading = repo.updateInfo.value
    assertEquals(com.example.model.ApkUpdateStatus.DOWNLOADING, downloading.status)
    assertEquals(0.5f, downloading.downloadProgress, 0.01f)
    assertEquals(21.4f, downloading.downloadedMb, 0.01f)

    // Test completion
    repo.completeApkInstallation()
    val completed = repo.updateInfo.value
    assertEquals(com.example.model.ApkUpdateStatus.UP_TO_DATE, completed.status)
    assertEquals(completed.latestVersion, completed.currentVersion)
  }
}

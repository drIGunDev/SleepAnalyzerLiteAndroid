package de.igor.gun.sleep.analyzer.math.bridges

// Bridge: ModelConfigurationParams (app-level type) -> HCModelConfigurationParams
// Uncomment when ModelConfigurationParams is available from the app module.

import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCModelConfigurationParams
import de.igor.gun.sleep.analyzer.misc.AppParameters


fun AppParameters.toHCModelConfigurationParams(): HCModelConfigurationParams =
    HCModelConfigurationParams(
        frameSizeHR = frameSizeHR.toDouble(),
        frameSizeACC = frameSizeACC.toDouble(),
        quantizationHR = quantizationHR.toDouble(),
        quantizationACC = quantizationACC.toDouble(),
        minSignificantIntervalSec = minSignificantIntervalSec,
        minAwakeDurationSec = minAwakeDurationSec,
        hrHiPassCutoff = hrHiPassCutoff,
        accHiPassCutoff = accHiPassCutoff
    )

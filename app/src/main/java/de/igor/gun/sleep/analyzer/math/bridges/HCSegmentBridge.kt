package de.igor.gun.sleep.analyzer.math.bridges

import android.graphics.PointF
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCPoint
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCSegment
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCSquareType
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.toPoints


fun List<HCSegment<HCSquareType>>.toPointFs(support: List<HCPoint>): List<PointF> =
    this.toPoints(support).toPointFs()

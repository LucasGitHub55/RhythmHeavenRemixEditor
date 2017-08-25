package io.github.chrislo27.rhre3.entity.model.multipart

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Equidistant
import io.github.chrislo27.rhre3.track.Remix


class EquidistantEntity(remix: Remix, datamodel: Equidistant)
    : MultipartEntity<Equidistant>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = datamodel.stretchable
    override val shouldRenderInternal = true

    override fun updateInternalCache(oldBounds: Rectangle) {
        translateInternal(oldBounds)
        var currentRelative: Float = 0f
        internal.forEachIndexed { index, it ->
            val track = (it.bounds.y - this.bounds.y).toInt()

            if (track == 0 && index > 0) {
                currentRelative += this@EquidistantEntity.bounds.width
            }

            it.bounds.x = this.bounds.x + currentRelative
            it.bounds.width = this.bounds.width
            it.bounds.y = this.bounds.y + track.toFloat()
        }
    }

    init {
        datamodel.cues.mapIndexedTo(internal) { index, pointer ->
            GameRegistry.data.objectMap[pointer.id]?.createEntity(remix)?.apply {
                this.updateBounds {
                    this.bounds.x = this@EquidistantEntity.bounds.x
                    this.bounds.y = this@EquidistantEntity.bounds.y + pointer.track
                    this.bounds.width = this@EquidistantEntity.bounds.width
                }

                // apply cue pointer settings
                (this as? IRepitchable)?.semitone = pointer.semitone
            } ?: error("Object with id ${pointer.id} not found")
        }
        this.bounds.width = datamodel.duration
        updateInternalCache(bounds)
    }

    override fun copy(remix: Remix): EquidistantEntity {
        return EquidistantEntity(remix, datamodel).also {
            it.semitone = this.semitone
            it.updateBounds {
                it.bounds.set(this@EquidistantEntity.bounds)
            }
        }
    }
}
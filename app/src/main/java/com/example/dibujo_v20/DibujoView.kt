package com.example.dibujo_v20

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DibujoView(contexto: Context, atributos: AttributeSet): View(contexto, atributos) {
    private lateinit var dibujarCamino: Camino
    private lateinit var canvasPaint: Paint
    private lateinit var pinturaPaint: Paint
    private var color = Color.BLACK
    private lateinit var canvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private var grosor = 0.0F
    private var caminos = mutableListOf<Camino>()

    init {
        inicioDibujo()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var coordX = event?.x
        var coordY = event?.y

        when(event?.action) {

            MotionEvent.ACTION_DOWN -> {
                dibujarCamino.color = color
                dibujarCamino.grosor = grosor.toFloat()

                dibujarCamino.reset()
                dibujarCamino.moveTo(coordX!!, coordY!!)
            }

            MotionEvent.ACTION_MOVE -> {
                dibujarCamino.lineTo(coordX!!, coordY!!)
            }

            MotionEvent.ACTION_UP -> {
                dibujarCamino = Camino(color, grosor)
                caminos.add(dibujarCamino)
            }

            else -> {
                return false
            }
        }
        // actualiza el layout para que se vean los cambios en pantalla
        invalidate()
        return true

    }

    override fun onDraw(canvas: Canvas) {

        canvas.drawBitmap(canvasBitmap, 0F, 0F, pinturaPaint)

        for (camino in caminos) {
            pinturaPaint.strokeWidth = camino.grosor
            pinturaPaint.color = camino.color
            canvas.drawPath(camino, pinturaPaint)
        }

        if (!dibujarCamino.isEmpty) {
            pinturaPaint.strokeWidth = dibujarCamino.grosor
            pinturaPaint.color = dibujarCamino.color
            canvas.drawPath(dibujarCamino, pinturaPaint)
        }
    }

    private fun inicioDibujo() {
        pinturaPaint = Paint()
        dibujarCamino = Camino(color, grosor)
        pinturaPaint.color = color
        pinturaPaint.style = Paint.Style.STROKE
        pinturaPaint.strokeJoin = Paint.Join.ROUND
        pinturaPaint.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)
        grosor = 20.0F
    }

    fun cambiarGrosor(nuevoTam: Float) {
        grosor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, nuevoTam, resources.displayMetrics)
        pinturaPaint.strokeWidth = grosor
    }

    fun cambiarColorPincel(nuevoColor: Any) {
        if (nuevoColor is String) {
            color = Color.parseColor(nuevoColor)
            pinturaPaint.color = color
        } else {
            color = nuevoColor as Int
            pinturaPaint.color = color
        }
    }

    fun deshacerCamino() {
        if (caminos.size > 0) {
            caminos.removeAt(caminos.size - 1)
            invalidate()
        }
    }

    inner class Camino(var color: Int, var grosor: Float): Path() {

    }

}
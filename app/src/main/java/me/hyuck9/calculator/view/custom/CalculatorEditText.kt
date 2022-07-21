package me.hyuck9.calculator.view.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.text.TextPaint
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import me.hyuck9.calculator.R
import timber.log.Timber

class CalculatorEditText : AppCompatEditText {

	private var maximumTextSize = 0f    // 텍스트 최대 크기
	private var minimumTextSize = 0f    // 텍스트 최소 크기
	private var stepTextSize = 0f       // 텍스트 크기 단계별 조절 크기

	private var widthConstraint = -1
	private var tempTextPaint = TextPaint()
	private var tempRect = Rect()

	private var onTextSizeChangeListener: OnTextSizeChangeListener? = null

	fun setOnTextSizeChangeListener(listener: OnTextSizeChangeListener) {
		onTextSizeChangeListener = listener
	}

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalculatorEditText, defStyleAttr, 0)
		maximumTextSize = typedArray.getDimension(R.styleable.CalculatorEditText_maxTextSize, textSize)
		minimumTextSize = typedArray.getDimension(R.styleable.CalculatorEditText_minTextSize, textSize)
		stepTextSize = typedArray.getDimension(R.styleable.CalculatorEditText_stepTextSize, (maximumTextSize - minimumTextSize) / 3)
		typedArray.recycle()
		initTextSize()
	}

	private fun initTextSize() {
		if ( isFocusable ) {
			movementMethod = ScrollingMovementMethod.getInstance()
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, maximumTextSize)	// 텍스트 크기 Default = maximumSize
		minHeight = lineHeight + compoundPaddingTop + compoundPaddingBottom
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		if ( event!!.actionMasked == MotionEvent.ACTION_UP ) {
			cancelLongPress()	// 클릭 시 키보드 노출 X
		}
		return super.onTouchEvent(event)
	}

	/**
	 * EditText에 쓰여질 Text와 EditText의 가로영역을 비교해
	 * minSize부터 stepSize를 더하며 최대 maxSize까지 비교한다.
	 * EditText의 가로영역보다 작은 Size로 최대한 큰 Size를 반환한다.
	 */
	fun getVariableTextSize(text: String): Float {
		if (widthConstraint < 0 || maximumTextSize <= minimumTextSize) {
			return textSize
		}
		tempTextPaint.set(paint)
		var lastFitTextSize = minimumTextSize
		while (lastFitTextSize < maximumTextSize) {
			val nextSize = (lastFitTextSize + stepTextSize).coerceAtMost(maximumTextSize)
			tempTextPaint.textSize = nextSize
			if (tempTextPaint.measureText(text) > widthConstraint) {
				break
			} else {
				lastFitTextSize = nextSize
			}
		}
		return lastFitTextSize
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		widthConstraint = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight	// 좌우패딩 제외한 실제 글자가 써질 가로 영역
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
	}

	override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter)
		val textLength = text?.length
		if (selectionStart != textLength || selectionEnd != textLength) {
			textLength?.let { setSelection(it) }
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
		Timber.i("textLength : $textLength, textSize : $textSize")
		Timber.i("getVariableTextSize : ${getVariableTextSize(text.toString())}")
		Timber.i("text : ${text.toString()}")
	}

	override fun setTextSize(unit: Int, size: Float) {
		val oldTextSize = textSize
		super.setTextSize(unit, size)
		onTextSizeChangeListener?.let {
			if (textSize != oldTextSize) {
				it.onTextSizeChanged(this, oldTextSize)	// 텍스트 사이즈가 기존과 다르면 onTextSizeChanged() 수행
			}
		}
	}

	override fun getCompoundPaddingTop(): Int {
		paint.getTextBounds("H", 0, 1, tempRect)
		val fontMetrics = paint.fontMetricsInt
		val paddingOffset = -(fontMetrics.ascent + tempRect.height())
		return super.getCompoundPaddingTop() - paddingTop.coerceAtMost(paddingOffset)
	}

	override fun getCompoundPaddingBottom(): Int {
		val fontMetrics = paint.fontMetricsInt
		return super.getCompoundPaddingBottom() - paddingBottom.coerceAtMost(fontMetrics.descent)
	}

	interface OnTextSizeChangeListener {
		fun onTextSizeChanged(textView: TextView, oldSize: Float)
	}

}
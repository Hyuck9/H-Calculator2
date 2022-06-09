package me.hyuck9.calculator.view.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout

class DraggablePanel(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

	private var isOpen = false
	private var initialY = 0f
	private var originalHeight = -1
	private var fullHeight = -1
	private var diffHeight = 0

	/**
	 * panel 드래그 시작 포지션
	 * range [0, 1] where 0 = closed, 1 = open
	 */
	private var dragOffset = 0f

	private val centerPoint = 0.5f
	private var panelSlideListener: PanelSlideListener? = null


	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
		if (originalHeight == -1) {
			originalHeight = measuredHeight				// 뷰의 높이
			diffHeight = fullHeight - originalHeight	// 부모뷰 높이 - 현재 뷰 높이 = 나머지 뷰의 높이
		}
		if (fullHeight == -1) {
			fullHeight = (parent as ViewGroup).height	// 부모 뷰그룹의 높이
			diffHeight = fullHeight - originalHeight	// 부모뷰 높이 - 현재 뷰 높이 = 나머지 뷰의 높이
		}
		super.onLayout(changed, l, t, r, b)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN -> {	// 뷰를 터치 했을 때
				initialY = event.y			// y좌표 변수 저장
			}
			MotionEvent.ACTION_UP -> {		// 터치 및 드래그 후 손이 떨어졌을 때
				val dy = event.y - initialY		// 손이 떨어진 Y좌표 (높이) - 처음 터치한 Y좌표 (높이) = 터치 후 드래그 한 거리 (높이) -> 위로 올리는 경우 음수, 아래로 내리는 경우 양수

				if (isOpen) {								// Panel이 열린(펴진) 상태인 경우
					if (-dy > diffHeight * centerPoint) {	// panel 나머지 뷰의 중앙 높이보다 높이 올렸을 경우
						smoothPanelClose(300)		// panel을 닫는다.
					} else {								// 그렇지 않으면
						smoothPanelOpen(300)		// panel을 다시 편다.
					}
				} else {									// Panel이 닫힌(접힌) 상태인 경우
					if (dy > diffHeight * centerPoint) {	// panel 나머지 뷰의 중앙 높이보다 낮게 내렸을 경우
						smoothPanelOpen(300)		// panel을 연다.
					} else {								// 그렇지 않으면
						smoothPanelClose(300)		// panel을 다시 접는다.
					}
				}
			}
			MotionEvent.ACTION_MOVE -> {	// 드래그 중일 때
				val y = event.y				// 현재 드래그중인 Y좌표 (높이)
				val dy = y - initialY		// 현재 드래그중인 Y좌표 (높이) - 처음 터치한 Y좌표 (높이) = 터치 후 현재까지 드래그 한 거리 (높이) -> 위로 올리는 경우 음수, 아래로 내리는 경우 양수
				if (isOpen) {			// Panel이 열린(펴진) 상태인 경우
					if (y > initialY) {	// 드래그중인 y좌표가 처음 터치한 좌표보다 아래로 내린 상태이면
						return true		// 아무것도 처리하지 않고 return
					} else if (fullHeight + dy > originalHeight) {	// 위로 드래그 중인 경우 (최대 원래 접힌 기본 높이까지)
						layoutParams = ViewGroup.LayoutParams(width, (fullHeight + dy).toInt())	// panel의 높이 조정
					}
				} else {				// Panel이 닫힌(접힌) 상태인 경우
					if (y < initialY) {	// 드래그중인 y좌표가 처음 터치한 좌표보다 위로 올린 상태이면
						return true		// 아무것도 처리하지 않고 return
					} else if (originalHeight + dy < fullHeight) {	// 아래로 드래그 중인 경우 (최대 부모뷰 높이까지)
						layoutParams = ViewGroup.LayoutParams(width, (originalHeight + dy).toInt())	// panel의 높이 조정
					}
				}
				dragOffset = (height - originalHeight).toFloat() / (fullHeight - originalHeight).toFloat()	// (커진만큼의 높이) 나누기 (나머지 뷰의 높이)를 dragOffset으로 설정
				dispatchOnPanelSlide(this)
			}
		}
		requestDisallowInterceptTouchEvent(true)	// 부모 뷰에 터치 이벤트 뺏기지 않도록 설정
		return true
	}


	/**
	 * panel 을 오픈한다. (펴기)
	 */
	@Suppress("SameParameterValue")
	private fun smoothPanelOpen(duration: Int) {
		isOpen = true
		ValueAnimator.ofInt(height, fullHeight).apply {
			this.addUpdateListener {
				layoutParams = ViewGroup.LayoutParams(width, animatedValue as Int)
			}
			this.duration = duration.toLong()
			this.interpolator = DecelerateInterpolator()		// 종료시점에 속도가 점점 감소
			this.start()
		}
		dragOffset = 1.0f
		dispatchOnPanelOpened(this)
	}

	/**
	 * panel 을 닫는다. (접기)
	 */
	fun smoothPanelClose(duration: Int) {
		isOpen = false
		ValueAnimator.ofInt(height, originalHeight).apply {
			this.addUpdateListener {
				layoutParams = ViewGroup.LayoutParams(width, animatedValue as Int)
			}
			this.duration = duration.toLong()
			this.interpolator = DecelerateInterpolator()		// 종료시점에 속도가 점점 감소
			this.start()
		}
		dragOffset = 0.0f
		dispatchOnPanelClosed(this)
	}

	fun isOpen(): Boolean {
		return isOpen
	}

	fun setPanelSlideListener(listener: PanelSlideListener?) {
		panelSlideListener = listener
	}


	interface PanelSlideListener {
		fun onPanelSlide(view: View, dragOffset: Float)
		fun onPanelOpened(view: View)
		fun onPanelClosed(view: View)
	}


	private fun dispatchOnPanelSlide(view: View) {
		panelSlideListener?.onPanelSlide(view, dragOffset)
	}

	private fun dispatchOnPanelOpened(view: View) {
		panelSlideListener?.onPanelOpened(view)
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
	}

	private fun dispatchOnPanelClosed(view: View) {
		panelSlideListener?.onPanelClosed(view)
		sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
	}
}
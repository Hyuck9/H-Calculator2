package me.hyuck9.calculator.data.db.entity

import android.text.Spanned
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.hyuck9.calculator.extensions.fromHtml
import me.hyuck9.calculator.extensions.makeCommaExpr
import me.hyuck9.calculator.extensions.toDateString
import me.hyuck9.calculator.extensions.toTimeString
import org.threeten.bp.LocalDateTime

@Entity
data class History(
	@ColumnInfo(name = "Expression")
	var expr: String,

	@ColumnInfo(name = "Answer")
	var answer: String,

	@ColumnInfo(name = "Date")
	var date: LocalDateTime = LocalDateTime.now(),
) {
	@PrimaryKey(autoGenerate = true)
	var id: Long = 0

	val formula: Spanned
		get() = expr.makeCommaExpr().fromHtml()

	val result: Spanned
		get() = "=${answer.makeCommaExpr()}".fromHtml()

	val createdTimeText: String
		get() = date.toTimeString()

	/*val createdDateText: String
		get() = date.toDateString()*/
}

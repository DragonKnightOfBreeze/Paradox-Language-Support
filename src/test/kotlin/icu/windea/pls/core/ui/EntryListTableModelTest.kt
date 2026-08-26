package icu.windea.pls.core.ui

import icu.windea.pls.core.util.Entry
import org.junit.Assert
import org.junit.Test

/**
 * @see EntryListTableModel
 */
class EntryListTableModelTest {
    @Test
    fun columnNamesAndValues_basic() {
        val list = mutableListOf(Entry("a", "b"))
        val model = EntryListTableModel(
            list = list,
            keyName = "Key",
            valueName = "Value",
            keyGetter = { it.uppercase() },
            keySetter = { it.lowercase() },
            valueGetter = { it.uppercase() },
            valueSetter = { it.lowercase() },
            valueAdder = { Entry("new", "new") },
        )
        Assert.assertEquals(2, model.columnCount)
        Assert.assertEquals("Key", model.getColumnName(0))
        Assert.assertEquals("Value", model.getColumnName(1))
        Assert.assertEquals("A", model.getValueAt(0, 0))
        Assert.assertEquals("B", model.getValueAt(0, 1))
    }

    @Test
    fun editableAndSetValue_basic() {
        val list = mutableListOf(Entry("a", "b"))
        val model = EntryListTableModel(
            list = list,
            keyName = "Key",
            valueName = "Value",
            keyGetter = { it.uppercase() },
            keySetter = { it.lowercase() },
            valueGetter = { it.uppercase() },
            valueSetter = { it.lowercase() },
            valueAdder = { Entry("", "") },
        )
        Assert.assertTrue(model.isCellEditable(0, 0))
        Assert.assertTrue(model.isCellEditable(0, 1))

        model.setValueAt("X", 0, 0)
        Assert.assertEquals("x", list[0].key)
        model.setValueAt("Y", 0, 1)
        Assert.assertEquals("y", list[0].value)
    }

    @Test
    fun editable_withoutSetter() {
        val list = mutableListOf(Entry("a", "b"))
        val model = EntryListTableModel(
            list = list,
            keyName = "Key",
            valueName = "Value",
            keyGetter = { it },
            keySetter = null,
            valueGetter = { it },
            valueSetter = null,
            valueAdder = null,
        )
        Assert.assertFalse(model.isCellEditable(0, 0))
        Assert.assertFalse(model.isCellEditable(0, 1))
    }

    @Test
    fun addRow_basic() {
        val list = mutableListOf(Entry("a", "b"))
        val model = EntryListTableModel(
            list = list,
            keyName = "Key",
            valueName = "Value",
            keyGetter = { it },
            keySetter = { it },
            valueGetter = { it },
            valueSetter = { it },
            valueAdder = { Entry("new", "new") },
        )
        model.addRow()
        Assert.assertEquals(2, model.rowCount)
        Assert.assertEquals("new", list[1].key)
        Assert.assertEquals("new", list[1].value)
    }

    @Test
    fun addRow_withoutAdder() {
        val list = mutableListOf(Entry("a", "b"))
        val model = EntryListTableModel(
            list = list,
            keyName = "Key",
            valueName = "Value",
            keyGetter = { it },
            keySetter = { it },
            valueGetter = { it },
            valueSetter = { it },
            valueAdder = null,
        )
        model.addRow()
        Assert.assertEquals(1, model.rowCount)
    }
}

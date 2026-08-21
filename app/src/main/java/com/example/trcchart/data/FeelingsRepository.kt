package com.example.trcchart.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class AppLanguage {
    ENGLISH,
    TAMIL
}

class FeelingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trc_chart_prefs", Context.MODE_PRIVATE)

    private val defaultFeelings = listOf(
        "Anger / கோபம்",
        "Fear / பயம்",
        "Sadness / துக்கம்",
        "Joy / ஆனந்தம்",
        "Jealousy / பொறாமை",
        "Anxiety / கவலை",
        "Peace / அமைதி",
        "Confusion / குழப்பம்",
        "Guilt / குற்றவுணர்ச்சி",
        "Pride / அகந்தை"
    )

    val checklistItems = listOf(
        // Section 1: தியானம் / MEDITATION (Section Code: 101)
        DailyChecklistItem(
            id = "sec101_1",
            textTa = "காலை தியானம் / Morning Meditation",
            textEn = "Morning Meditation / காலை தியானம்",
            section = 101
        ),
        DailyChecklistItem(
            id = "sec101_2",
            textTa = "மாலை தியானம் / Evening Meditation",
            textEn = "Evening Meditation / மாலை தியானம்",
            section = 101
        ),

        // Section 2: சுத்திகரிப்பு செயல்முறை / CLEANING PROCESS (Section Code: 102)
        DailyChecklistItem(
            id = "sec102_1",
            textTa = "காலை சுத்திகரிப்பு செயல்முறை / Morning Cleaning Process",
            textEn = "Morning Cleaning Process / காலை சுத்திகரிப்பு செயல்முறை",
            section = 102
        ),
        DailyChecklistItem(
            id = "sec102_2",
            textTa = "மாலை சுத்திகரிப்பு செயல்முறை / Evening Cleaning Process",
            textEn = "Evening Cleaning Process / மாலை சுத்திகரிப்பு செயல்முறை",
            section = 102
        ),

        // Section 3: அன்பு / LOVE (Section Code: 1)
        DailyChecklistItem(
            id = "sec1_1",
            textTa = "அன்பென்பது சாதி, மதம், இனம், மொழி, நிறம் கடந்தது.\nLOVE IS UNCONDITIONAL, IT GOES BEYOND CASTE, CREED, COLOUR, RACE, RELIGION AND GENDER",
            textEn = "LOVE IS UNCONDITIONAL, IT GOES BEYOND CASTE, CREED, COLOUR, RACE, RELIGION AND GENDER",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_2",
            textTa = "அன்பென்பது எதிர்பார்ப்பு இல்லாதது.\nLOVE IS DOING WITHOUT EXPECTATIONS AND JUDGEMENTS",
            textEn = "LOVE IS DOING WITHOUT EXPECTATIONS AND JUDGEMENTS",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_3",
            textTa = "அன்பென்பது மற்றவர் இருக்கும் நிலையிலேயே ஏற்றுக்கொள்வது.\nLOVE IS ACCEPTING THE OTHER PARTY AS WHAT SHE/HE IS",
            textEn = "LOVE IS ACCEPTING THE OTHER PARTY AS WHAT SHE/HE IS",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_4",
            textTa = "அன்பென்பது கொடுத்து உணர்வது.\nLOVE IS GIVING AND MAKING THE OTHER PARTY REALIZE",
            textEn = "LOVE IS GIVING AND MAKING THE OTHER PARTY REALIZE",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_5",
            textTa = "அன்பென்பது பிறரிடம் நன்மையையே பார்ப்பது.\nLOVE IS LOOKING FOR GOOD QUALITIES ON TO OTHERS",
            textEn = "LOVE IS LOOKING FOR GOOD QUALITIES ON TO OTHERS",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_6",
            textTa = "அன்பென்பது நாமும் நன்றாக இருக்க வேண்டும் பிறரும் நன்றாக இருக்க வேண்டும் என்று எண்ணுவது.\nLOVE IS A WIN SITUATION",
            textEn = "LOVE IS A WIN SITUATION",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_7",
            textTa = "அன்பென்பது மற்றவர் உணர்வுக்கு மதிப்பளிப்பது.\nLOVE IS RESPECTING OTHERS FEELING",
            textEn = "LOVE IS RESPECTING OTHERS FEELING",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_8",
            textTa = "அன்பென்பது நன்றியோடு இருத்தல்.\nLOVE IS FOR GIVING AND FOR GETTING WITH GRATITUDE",
            textEn = "LOVE IS FOR GIVING AND FOR GETTING WITH GRATITUDE",
            section = 1
        ),
        DailyChecklistItem(
            id = "sec1_9",
            textTa = "அன்பென்பது தெய்வமானது.\nLOVE IS DIVINE",
            textEn = "LOVE IS DIVINE",
            section = 1
        ),

        // Section 4: கணவன் மனைவி / HUSBAND & WIFE (Section Code: 2)
        DailyChecklistItem(
            id = "sec2_1",
            textTa = "கணவன் மனைவியின் / மனைவி கணவனின் நல்ல குணங்களை எழுதி அன்றாடம் பார்ப்பது.\nHUSBAND AND WIFE WRITING GOOD ATTRIBUTES OF EACH OTHER AND SEEING THEM DAILY",
            textEn = "HUSBAND AND WIFE WRITING GOOD ATTRIBUTES OF EACH OTHER AND SEEING THEM DAILY",
            section = 2
        ),
        DailyChecklistItem(
            id = "sec2_2",
            textTa = "கணவன் மனைவி காலையில் எழுந்தவுடன் ஒருவரை ஒருவர் பார்த்து புன்னகைப்பது.\nHUSBAND AND WIFE SMILING UPON EACH OTHER AFTER WAKING IN THE MORNING",
            textEn = "HUSBAND AND WIFE SMILING UPON EACH OTHER AFTER WAKING IN THE MORNING",
            section = 2
        ),
        DailyChecklistItem(
            id = "sec2_3",
            textTa = "கணவன் மனைவி ஒருவரை ஒருவர் பாராட்டுக.\nHUSBAND AND WIFE APPRECIATING EACH OTHER",
            textEn = "HUSBAND AND WIFE APPRECIATING EACH OTHER",
            section = 2
        ),
        DailyChecklistItem(
            id = "sec2_4",
            textTa = "கணவன் மனைவி தவறு செய்தால் ஒருவரிடம் ஒருவர் மன்னிப்பு கேட்டு திருந்துவது தவற செய்யாதிருப்பது.\nHUSBAND AND WIFE IF COMMITTED A MISTAKE, ASKING SORRY TO EACH OTHER AND NOT REPEATING IT",
            textEn = "HUSBAND AND WIFE IF COMMITTED A MISTAKE, ASKING SORRY TO EACH OTHER AND NOT REPEATING IT",
            section = 2
        ),
        DailyChecklistItem(
            id = "sec2_5",
            textTa = "கணவன் மனைவி இருவரில் ஒருவர் பேசும் போது மற்றவர் கேட்டு அமைதியாக இருந்து பிறகு பேசுவது.\nHUSBAND AND WIFE WHEN TALKING TO EACH OTHER ONE SHOULD REMAIN SILENT AND THEN TALK TO THE OTHER",
            textEn = "HUSBAND AND WIFE WHEN TALKING TO EACH OTHER ONE SHOULD REMAIN SILENT AND THEN TALK TO THE OTHER",
            section = 2
        ),
        DailyChecklistItem(
            id = "sec2_6",
            textTa = "கணவன் மனைவி இருவருக்குள்ளும் விட்டுக் கொடுப்பது.\nHUSBAND AND WIFE SHOULD GIVE AND TAKE",
            textEn = "HUSBAND AND WIFE SHOULD GIVE AND TAKE",
            section = 2
        ),

        // Section 5: மனப்பாங்கு / ATTITUDE & QUALITIES (Section Code: 3)
        DailyChecklistItem(
            id = "sec3_1",
            textTa = "அன்பு / Loving",
            textEn = "Loving",
            section = 3
        ),
        DailyChecklistItem(
            id = "sec3_2",
            textTa = "அக்கறை / Caring",
            textEn = "Caring",
            section = 3
        ),
        DailyChecklistItem(
            id = "sec3_3",
            textTa = "பணிவு / Humble",
            textEn = "Humble",
            section = 3
        ),
        DailyChecklistItem(
            id = "sec3_4",
            textTa = "பொறுமை / Patience",
            textEn = "Patience",
            section = 3
        ),
        DailyChecklistItem(
            id = "sec3_5",
            textTa = "நம்பிக்கை / Self Confidence",
            textEn = "Self Confidence",
            section = 3
        )
    )

    private val _feelings = MutableStateFlow<List<String>>(emptyList())
    val feelings: StateFlow<List<String>> = _feelings.asStateFlow()

    private val _entries = MutableStateFlow<List<TRCEntry>>(emptyList())
    val entries: StateFlow<List<TRCEntry>> = _entries.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _checkedChecklistIds = MutableStateFlow<Set<String>>(emptySet())
    val checkedChecklistIds: StateFlow<Set<String>> = _checkedChecklistIds.asStateFlow()

    // Section Visibility Preferences (Default all true)
    private val _showMeditation = MutableStateFlow(true)
    val showMeditation: StateFlow<Boolean> = _showMeditation.asStateFlow()

    private val _showCleaning = MutableStateFlow(true)
    val showCleaning: StateFlow<Boolean> = _showCleaning.asStateFlow()

    private val _showSection1 = MutableStateFlow(true)
    val showSection1: StateFlow<Boolean> = _showSection1.asStateFlow()

    private val _showSection2 = MutableStateFlow(true)
    val showSection2: StateFlow<Boolean> = _showSection2.asStateFlow()

    private val _showSection3 = MutableStateFlow(true)
    val showSection3: StateFlow<Boolean> = _showSection3.asStateFlow()

    init {
        loadLanguage()
        loadFeelings()
        loadEntries()
        loadChecklist()
        loadSectionVisibility()
    }

    private fun loadLanguage() {
        val langStr = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
        _language.value = try {
            AppLanguage.valueOf(langStr ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    private fun loadSectionVisibility() {
        _showMeditation.value = prefs.getBoolean(KEY_SHOW_MEDITATION, true)
        _showCleaning.value = prefs.getBoolean(KEY_SHOW_CLEANING, true)
        _showSection1.value = prefs.getBoolean(KEY_SHOW_SEC_1, true)
        _showSection2.value = prefs.getBoolean(KEY_SHOW_SEC_2, true)
        _showSection3.value = prefs.getBoolean(KEY_SHOW_SEC_3, true)
    }

    fun setMeditationVisibility(visible: Boolean) {
        _showMeditation.value = visible
        prefs.edit().putBoolean(KEY_SHOW_MEDITATION, visible).apply()
    }

    fun setCleaningVisibility(visible: Boolean) {
        _showCleaning.value = visible
        prefs.edit().putBoolean(KEY_SHOW_CLEANING, visible).apply()
    }

    fun setSection1Visibility(visible: Boolean) {
        _showSection1.value = visible
        prefs.edit().putBoolean(KEY_SHOW_SEC_1, visible).apply()
    }

    fun setSection2Visibility(visible: Boolean) {
        _showSection2.value = visible
        prefs.edit().putBoolean(KEY_SHOW_SEC_2, visible).apply()
    }

    fun setSection3Visibility(visible: Boolean) {
        _showSection3.value = visible
        prefs.edit().putBoolean(KEY_SHOW_SEC_3, visible).apply()
    }

    private fun loadFeelings() {
        val jsonStr = prefs.getString(KEY_FEELINGS_JSON, null)
        if (jsonStr != null) {
            try {
                _feelings.value = Json.decodeFromString<List<String>>(jsonStr)
            } catch (e: Exception) {
                _feelings.value = defaultFeelings
            }
        } else {
            val saved = prefs.getStringSet(KEY_FEELINGS, null)
            if (saved == null) {
                _feelings.value = defaultFeelings
                saveFeelingsToPrefs(defaultFeelings)
            } else {
                _feelings.value = saved.toList()
                saveFeelingsToPrefs(_feelings.value)
            }
        }
    }

    private fun saveFeelingsToPrefs(list: List<String>) {
        try {
            val jsonStr = Json.encodeToString(list)
            prefs.edit().putString(KEY_FEELINGS_JSON, jsonStr).apply()
        } catch (e: Exception) {
            prefs.edit().putStringSet(KEY_FEELINGS, list.toSet()).apply()
        }
    }

    fun addFeeling(feeling: String): Boolean {
        val trimmed = feeling.trim()
        if (trimmed.isEmpty()) return false
        val current = _feelings.value.toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return false
        current.add(trimmed)
        _feelings.value = current
        saveFeelingsToPrefs(current)
        return true
    }

    fun editFeeling(oldFeeling: String, newFeeling: String): Boolean {
        val trimmed = newFeeling.trim()
        if (trimmed.isEmpty()) return false
        val current = _feelings.value.toMutableList()
        val index = current.indexOf(oldFeeling)
        if (index == -1) return false
        current[index] = trimmed
        _feelings.value = current
        saveFeelingsToPrefs(current)
        return true
    }

    fun removeFeeling(feeling: String): Boolean {
        val current = _feelings.value.toMutableList()
        if (current.remove(feeling)) {
            _feelings.value = current
            saveFeelingsToPrefs(current)
            return true
        }
        return false
    }

    fun moveFeelingUp(index: Int): Boolean {
        if (index > 0 && index < _feelings.value.size) {
            val current = _feelings.value.toMutableList()
            val item = current.removeAt(index)
            current.add(index - 1, item)
            _feelings.value = current
            saveFeelingsToPrefs(current)
            return true
        }
        return false
    }

    fun moveFeelingDown(index: Int): Boolean {
        if (index >= 0 && index < _feelings.value.size - 1) {
            val current = _feelings.value.toMutableList()
            val item = current.removeAt(index)
            current.add(index + 1, item)
            _feelings.value = current
            saveFeelingsToPrefs(current)
            return true
        }
        return false
    }

    private fun loadEntries() {
        val jsonStr = prefs.getString(KEY_ENTRIES, null)
        if (jsonStr != null) {
            try {
                _entries.value = Json.decodeFromString<List<TRCEntry>>(jsonStr)
            } catch (e: Exception) {
                _entries.value = emptyList()
            }
        }
    }

    fun addEntry(entry: TRCEntry) {
        val current = _entries.value.toMutableList()
        current.add(0, entry) // latest first
        _entries.value = current
        saveEntriesToPrefs(current)
    }

    fun updateEntry(updatedEntry: TRCEntry): Boolean {
        val current = _entries.value.toMutableList()
        val index = current.indexOfFirst { it.id == updatedEntry.id }
        if (index != -1) {
            current[index] = updatedEntry
            _entries.value = current
            saveEntriesToPrefs(current)
            return true
        }
        return false
    }

    fun deleteEntry(entryId: String): Boolean {
        val current = _entries.value.toMutableList()
        if (current.removeIf { it.id == entryId }) {
            _entries.value = current
            saveEntriesToPrefs(current)
            return true
        }
        return false
    }

    private fun saveEntriesToPrefs(list: List<TRCEntry>) {
        try {
            val jsonStr = Json.encodeToString(list)
            prefs.edit().putString(KEY_ENTRIES, jsonStr).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadChecklist() {
        val savedSet = prefs.getStringSet(KEY_CHECKLIST, null)
        _checkedChecklistIds.value = savedSet ?: emptySet()
    }

    fun toggleChecklistItem(id: String) {
        val current = _checkedChecklistIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _checkedChecklistIds.value = current
        prefs.edit().putStringSet(KEY_CHECKLIST, current).apply()
    }

    // Export current application data as JSON Backup string
    fun exportBackupJson(): String {
        val backup = BackupData(
            feelings = _feelings.value,
            entries = _entries.value,
            checkedChecklistIds = _checkedChecklistIds.value.toList(),
            showSection1 = _showSection1.value,
            showSection2 = _showSection2.value,
            showSection3 = _showSection3.value,
            language = _language.value.name
        )
        return Json.encodeToString(backup)
    }

    // Import backup JSON string into local application storage
    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val backup = Json.decodeFromString<BackupData>(jsonString)

            _feelings.value = backup.feelings.sorted()
            saveFeelingsToPrefs(_feelings.value)

            _entries.value = backup.entries.sortedByDescending { it.timestamp }
            saveEntriesToPrefs(_entries.value)

            _checkedChecklistIds.value = backup.checkedChecklistIds.toSet()
            prefs.edit().putStringSet(KEY_CHECKLIST, _checkedChecklistIds.value).apply()

            setSection1Visibility(backup.showSection1)
            setSection2Visibility(backup.showSection2)
            setSection3Visibility(backup.showSection3)

            val lang = try { AppLanguage.valueOf(backup.language) } catch (e: Exception) { AppLanguage.ENGLISH }
            setLanguage(lang)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        private const val KEY_FEELINGS = "key_feelings_list"
        private const val KEY_FEELINGS_JSON = "key_feelings_list_json"
        private const val KEY_ENTRIES = "key_trc_entries"
        private const val KEY_LANGUAGE = "key_app_language"
        private const val KEY_CHECKLIST = "key_checklist_checked"
        private const val KEY_SHOW_MEDITATION = "key_show_meditation"
        private const val KEY_SHOW_CLEANING = "key_show_cleaning"
        private const val KEY_SHOW_SEC_1 = "key_show_section_1"
        private const val KEY_SHOW_SEC_2 = "key_show_section_2"
        private const val KEY_SHOW_SEC_3 = "key_show_section_3"
    }
}

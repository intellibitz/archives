package intellibitz.intellidroid.util

enum class MediaPickerRequest {
    REQUEST_CAPTURE,
    REQUEST_GALLERY,
    REQUEST_DOCUMENTS,
    REQUEST_CROP,
    REQUEST_CHOOSER;

    companion object {
        /**
         * Convert request code integer back into a user
         * friendly enum.
         *
         * @param code Request code integer.
         * @return Request enum.
         */
        @JvmStatic
        fun create(code: Int): MediaPickerRequest? {
            return when (code) {
                777 -> REQUEST_CAPTURE
                778 -> REQUEST_GALLERY
                779 -> REQUEST_DOCUMENTS
                800 -> REQUEST_CROP
                801 -> REQUEST_CHOOSER
                else -> null
            }
        }
    }

    /**
     * Internally get the associated request code to used in
     * the activity intent system.
     *
     * @return Unique request code for each operation.
     */
    val code: Int
        get() = when (this) {
            REQUEST_CAPTURE -> 777
            REQUEST_GALLERY -> 778
            REQUEST_DOCUMENTS -> 779
            REQUEST_CROP -> 800
            REQUEST_CHOOSER -> 801
        }
}

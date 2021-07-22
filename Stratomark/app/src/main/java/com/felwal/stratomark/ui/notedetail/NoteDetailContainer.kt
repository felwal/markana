package com.felwal.stratomark.ui.notedetail

import com.felwal.stratomark.data.NoteRepository

class NoteDetailContainer(repo: NoteRepository) {

    val noteDetailViewModel = NoteDetailViewModel(repo)
}
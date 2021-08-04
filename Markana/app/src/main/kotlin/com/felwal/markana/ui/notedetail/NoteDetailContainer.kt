package com.felwal.markana.ui.notedetail

import com.felwal.markana.data.NoteRepository

class NoteDetailContainer(repo: NoteRepository) {

    val noteDetailViewModel = NoteDetailViewModel(repo)
}
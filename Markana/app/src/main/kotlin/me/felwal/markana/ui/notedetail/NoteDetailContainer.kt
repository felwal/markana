package me.felwal.markana.ui.notedetail

import me.felwal.markana.data.NoteRepository

class NoteDetailContainer(repo: NoteRepository) {

    val noteDetailViewModel = NoteDetailViewModel(repo)
}
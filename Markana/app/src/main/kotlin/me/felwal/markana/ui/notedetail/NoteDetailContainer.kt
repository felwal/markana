package me.felwal.markana.ui.notedetail

import me.felwal.markana.data.Repository

class NoteDetailContainer(repo: Repository) {

    val noteDetailViewModel = NoteDetailViewModel(repo)
}
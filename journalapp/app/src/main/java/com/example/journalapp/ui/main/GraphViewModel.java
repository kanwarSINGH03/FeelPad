package com.example.journalapp.ui.main;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.journalapp.database.NoteRepository;
import com.example.journalapp.database.entity.Note;
import java.util.List;

public class GraphViewModel extends AndroidViewModel {

    private final NoteRepository noteRepository;
    private final LiveData<List<Note>> allNotesLiveData;

    public GraphViewModel(Application application) {
        super(application);
        noteRepository = NoteRepository.getInstance(application);
        allNotesLiveData = noteRepository.getAllNotesOrderedByLastEditedDateDesc();
    }

    public LiveData<List<Note>> getAllNotesLiveData() {
        return allNotesLiveData;
    }
}

package com.example.journalapp.ui.note;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.journalapp.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;

/**
 * Fragment that receives a PDF Uri and opens it via a Pdfviewer
 */
public class PdfViewerFragment extends DialogFragment {

    private TextView pageNumView;
    private ImageButton backButton;
    private ImageButton annotateButton;
    private Uri pdfUri;
    private static final String ARG_PDF_URI = "pdf_uri";

    /**
     * Required constructor
     */
    public PdfViewerFragment() {
    }

    /**
     * Creates a new isntance of the fragment using pdfUri
     * Note: Fragments usually uses newInstance rather than a constructor
     * @param pdfUri
     * @return
     */
    public static PdfViewerFragment newInstance(Uri pdfUri) {
        PdfViewerFragment fragment = new PdfViewerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PDF_URI, pdfUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of the fragment
     * Extracts pdf URI from the arguments
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pdfUri = Uri.parse(getArguments().getString(ARG_PDF_URI));
        }
        setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialogFragmentStyle); // allows background to be transparent
    }

    /**
     * Called to have fragment instantiate the UI View
     * Initialzies the PDF Viewer and sets up components
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);
        PDFView pdfView = view.findViewById(R.id.pdfView);
        pageNumView = view.findViewById(R.id.pageNumber);
        backButton = view.findViewById(R.id.backButtonPdf);
        annotateButton = view.findViewById(R.id.annotateButtonPdf);


        // Initializes the PDF Viewer
        try {
            Log.d("PdfViewerFragment", "Loading PDF Uri: " + pdfUri);
            pdfView.fromUri(pdfUri)
                    .defaultPage(0)
                    .fitEachPage(true)
                    .pageSnap(true)
                    .enableSwipe(true)
                    .swipeHorizontal(true)
                    .pageFitPolicy(FitPolicy.BOTH)
                    .onPageChange((page, pageCount) -> pageNumView.setText(String.format("Page %d of %d", page + 1, pageCount)))
                    .load();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PdfViewerFragment", "Error loading PDF", e);

        }

        // Back button
        backButton.setOnClickListener(v -> {
            dismiss();
        });

        // Annotate Pdf Button
        annotateButton.setOnClickListener(v ->{
            // Use an intent to open the PDF in an external app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // Get the Context from the view
            Context context = v.getContext();

            // Check if there is an app that can handle this intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.e("PDF", "Opening PDF in external app");
            } else {
                // Inform the user if no application can handle the PDF
                Toast.makeText(context, "No application available to open PDF", Toast.LENGTH_SHORT).show();
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.adobe.reader")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader")));
                }
                Toast.makeText(context, "Redirecting to the Play Store for a PDF annotation app", Toast.LENGTH_SHORT).show();


            }
        });

        return view;
    }

    /**
     * Called when fragment becomes visible
     * Sets layout of dialog window to match parent dimensions
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
    }
}


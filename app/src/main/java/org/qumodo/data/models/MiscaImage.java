package org.qumodo.data.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Map;

public class MiscaImage implements ClusterItem {

    private String id;
    private String path;
    private String classifier;
    private String captions;
    private LatLng latLng;
    private Map<String, String> exifData;

    public MiscaImage(String id, String path, String classifier, String captions, LatLng latLng, Map<String, String> exifData) {
        this.id = id;
        this.path = path;
        this.classifier = classifier;
        this.captions = captions;
        this.latLng = latLng;
        this.exifData = exifData;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getCaptions() {
        return captions;
    }

    public Map<String, String> getExifData() {
        return exifData;
    }

    public boolean conformsToClassification(String classification) {
        String[] terms = classification.split(" ");
        boolean found = false;
        for (String term : terms) {
            found = found || classifier.contains(term) || captions.contains(term);
        }

        return found;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return classifier;
    }

    @Override
    public String getSnippet() {
        return captions;
    }
}

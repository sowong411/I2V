package com.example.onzzz.i2v;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by hoyuichan on 1/21/2016.
 */
public class Photo implements Comparable<Photo> {

    private opencv_core.Mat mat;
    private String photoString;
    private String photoPath;
    //  For selection
    private boolean isBlur;
    private double size;
    private double resolution;


    // For Sorting
    private double levelOfSmile;
    //private String time;   (refer to creation or upload )
    private int numberOfFace;
    private double averageAge;
    private double varianceAge;
    private double genderRatio;
    private double meanOfAge;
    private double varianceOfAge;
    private int facePosition;

    // For choosing template


    Photo(){
        //isBlur = false;
        //size= 0;
        //resolution= 0;
        //private String time;   (refer to creation or upload )
        mat = null;
        photoPath = null;
        levelOfSmile = 0;
        numberOfFace = 0;
        averageAge = 0;
        varianceAge = 0;
        genderRatio = 0;
        meanOfAge = 0;
        varianceOfAge = 0;
        facePosition = 0;
    }

    public Photo(String photoString) {
        this.photoString = photoString;
    }

    public Photo(String photoString, int numOfFace, double averageSmile, double averageAge, double varianceAge, double genderRatio, int facePosition) {
        this.photoPath = null;
        this.photoString = photoString;
        this.numberOfFace = numOfFace;
        this.levelOfSmile = averageSmile;
        this.averageAge = averageAge;
        this.varianceAge = varianceAge;
        this.genderRatio = genderRatio;
        this.facePosition = facePosition;
    }


    //Set Functions
    public void setPhotoString(String s){this.photoString = s;}
    public void setLevelOfSmile (double d){this.levelOfSmile = d;}
    public void  setNumberOfFace(int i){this.numberOfFace = i;}
    public void setAverageAge(double d){this.averageAge = d;}
    public void setVarianceAge(double d){this.varianceAge = d;}
    public void setGenderRatio(double d){this.genderRatio = d;}
    public void setMeanOfAge (double d){ this.meanOfAge = d;}
    public void setVarianceOfAge (double d){ this.varianceOfAge = d;}
    public void setPhotoPath(String photoPath) {this.photoPath = photoPath;}
    public void setMat(opencv_core.Mat mat) {this.mat = mat;}
    public void setFacePosition(int facePosition) {
        this.facePosition = facePosition;
    }

    //Get Functions
    public String getPhotoString(){return photoString;}
    public double getLevelOfSmile (){return levelOfSmile ;}
    public int  getNumberOfFace(){return numberOfFace ;}
    public double getAverageAge(){return averageAge;}
    public double getVarianceAge(){return varianceAge;}
    public double getGenderRatio(){return genderRatio;}
    public double getMeanOfAge ( ){ return meanOfAge ;}
    public double getVarianceOfAge (){ return varianceOfAge;}
    public String getPhotoPath() {return photoPath;}
    public int getFacePosition() {return facePosition;}
    public opencv_core.Mat getMat() {return mat;}


    @Override
    public int compareTo(Photo another) {
        double a = this.getLevelOfSmile();
        double b = another.getLevelOfSmile();
        if (a < b) {return 1;}
        if (a > b) {return -1;}
        return 0;
    }
}

package com.example.onzzz.i2v;

/**
 * Created by hoyuichan on 1/21/2016.
 */
public class Photo {

    //  For selection
    private boolean isBlur;
    private double size;
    private double resolution;


    // For Sorting
    private double levelOfSmile;
    //private String time;   (refer to creation or upload )
    private int numberOfFace;
    private int male;
    private int female;
    private double meanOfAge;
    private double varianceOfAge;

    // For choosing template


    Photo(){
        //isBlur = false;
        //size= 0;
        //resolution= 0;
        //private String time;   (refer to creation or upload )
        levelOfSmile = 0;
        numberOfFace = 0;
        male = 0;
        female = 0;
        meanOfAge = 0;
        varianceOfAge = 0;
    }


    //Set Functions
    public void setLevelOfSmile (double d){this.levelOfSmile = d;}
    public void  setNumberOfFace(int i){this.numberOfFace = i;}
    public void setNumberOfMale(int i){ this.male= i;}
    public void setNumberOfFemale(int i){ this.female= i;}
    public void setMeanOfAge (double d){ this.meanOfAge = d;}
    public void setVarianceOfAge (double d){ this.varianceOfAge = d;}

    //Get Functions
    public double getLevelOfSmile (){return levelOfSmile ;}
    public int  getNumberOfFace(){return numberOfFace ;}
    public int  getNumberOfMale(){ return male;}
    public  int  getNumberOfFemale(){return female;}
    public double getMeanOfAge ( ){ return meanOfAge ;}
    public double getVarianceOfAge (){ return varianceOfAge;}

}

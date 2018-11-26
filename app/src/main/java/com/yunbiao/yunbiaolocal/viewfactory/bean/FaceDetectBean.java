package com.yunbiao.yunbiaolocal.viewfactory.bean;

import java.util.List;

public class FaceDetectBean {
    /**
     * {
        "image_id": "hYdFjGNXXPffF4YHt0ottQ==",
            "request_id": "1524641077,231db415-1db6-431e-9503-070457d9384d",
            "time_used": 249,
            "faces": [{
        "attributes": {
            "gender": {
                "value": "Male"
            },
            "age": {
                "value": 25
            },
            "smile": {
                "threshold": 50.0,
                        "value": 0.023
            },
            "glass": {
                "value": "None"
            }
        },
        "face_rectangle": {
            "width": 36,
                    "top": 318,
                    "left": 409,
                    "height": 36
        },
        "face_token": "19203d7493d080209199a0481c4ddeba"
    }]
    }
     **/

    private List<FacesBean> faces;

    public List<FacesBean> getFaces() {
        return faces;
    }

    public void setFaces(List<FacesBean> faces) {
        this.faces = faces;
    }

    public static class FacesBean {
        private AttributesBean attributes;

        public AttributesBean getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesBean attributes) {
            this.attributes = attributes;
        }

        public static class AttributesBean {

            private AgeBean age;
            private GenderBean gender;
            private GlassBean glass;
            private SmileBean smile;

            public AgeBean getAge() {
                return age;
            }

            public void setAge(AgeBean age) {
                this.age = age;
            }

            public GenderBean getGender() {
                return gender;
            }

            public void setGender(GenderBean gender) {
                this.gender = gender;
            }

            public GlassBean getGlass() {
                return glass;
            }

            public void setGlass(GlassBean glass) {
                this.glass = glass;
            }

            public SmileBean getSmile() {
                return smile;
            }

            public void setSmile(SmileBean smile) {
                this.smile = smile;
            }

            public static class AgeBean {
                private int value;

                public int getValue() {
                    return value;
                }

                public void setValue(int value) {
                    this.value = value;
                }
            }

            public static class GenderBean {
                private String value;

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class GlassBean {
                private String value;

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class SmileBean {
                private double threshold;
                private double value;

                public double getThreshold() {
                    return threshold;
                }

                public void setThreshold(double threshold) {
                    this.threshold = threshold;
                }

                public double getValue() {
                    return value;
                }

                public void setValue(double value) {
                    this.value = value;
                }
            }
        }
    }
}

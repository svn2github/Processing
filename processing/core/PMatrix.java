package processing.core;

public final class PMatrix  implements PConstants {
    final static int DEFAULT_STACK_DEPTH = 0;

    float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

    float tmpx, tmpy, tmpz, tmpw;
    float stack[][];
    float resetValue[];
    int   stackPointer = 0;
    int   angleMode;
    int   maxStackDepth;

  public PMatrix(int angleMode) {
      this.angleMode = angleMode;
      set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
      maxStackDepth = DEFAULT_STACK_DEPTH;
    }

  public PMatrix(int angleMode, int stackDepth) {
      this.angleMode = angleMode;
      set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
      stack = new float[stackDepth][16];
      maxStackDepth = stackDepth;
    }

  public PMatrix(int angleMode,
           float m00, float m01, float m02, float m03,
           float m10, float m11, float m12, float m13,
           float m20, float m21, float m22, float m23,
           float m30, float m31, float m32, float m33) {
      this.angleMode = angleMode;
      set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
      maxStackDepth = DEFAULT_STACK_DEPTH;
    }

    // Make a copy of a matrix. We copy the stack depth,
    // but we don't make a copy of the stack or the stack pointer.
  public PMatrix(PMatrix src) {
      angleMode = src.angleMode;
      set(src.m00, src.m01, src.m02, src.m03,
          src.m10, src.m11, src.m12, src.m13,
          src.m20, src.m21, src.m22, src.m23,
          src.m30, src.m31, src.m32, src.m33);
      maxStackDepth = src.maxStackDepth;
      stack = new float[maxStackDepth][16];
    }

    void angleMode(int angleMode) {
      this.angleMode = angleMode;
    }

    void identity() {
      set(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }

    void reset() {
      if (resetValue == null) {
        identity();
      }
      else {
        set(resetValue[0], resetValue[1], resetValue[2],resetValue[3],
            resetValue[4], resetValue[5], resetValue[6], resetValue[7],
            resetValue[8], resetValue[9], resetValue[10], resetValue[11],
            resetValue[12], resetValue[13], resetValue[14], resetValue[15]);
      }
    }

    void storeResetValue() {
      if (resetValue == null) {
        resetValue = new float[16];
      }
      resetValue[0]  = m00; resetValue[1]  = m01; resetValue[2]  = m02; resetValue[3]  = m03;
      resetValue[4]  = m10; resetValue[5]  = m11; resetValue[6]  = m12; resetValue[7]  = m13;
      resetValue[8]  = m20; resetValue[9]  = m21; resetValue[10] = m22; resetValue[11] = m23;
      resetValue[12] = m30; resetValue[13] = m31; resetValue[14] = m32; resetValue[15] = m33;
    }

    void clearStack() {
      stackPointer = 0;
    }

    boolean push() {
      if (stackPointer == maxStackDepth) return false;

      stack[stackPointer][0] = m00; stack[stackPointer][1] = m01; stack[stackPointer][2] = m02; stack[stackPointer][3] = m03;
      stack[stackPointer][4] = m10; stack[stackPointer][5] = m11; stack[stackPointer][6] = m12; stack[stackPointer][7] = m13;
      stack[stackPointer][8] = m20; stack[stackPointer][9] = m21; stack[stackPointer][10] = m22; stack[stackPointer][11] = m23;
      stack[stackPointer][12] = m30; stack[stackPointer][13] = m31; stack[stackPointer][14] = m32; stack[stackPointer][15] = m33;
      stackPointer++;
      return true;
    }

    boolean pop() {
      if (stackPointer == 0) return false;

      stackPointer--;
      m00 = stack[stackPointer][0]; m01 = stack[stackPointer][1]; m02 = stack[stackPointer][2]; m03 = stack[stackPointer][3];
      m10 = stack[stackPointer][4]; m11 = stack[stackPointer][5]; m12 = stack[stackPointer][6]; m13 = stack[stackPointer][7];
      m20 = stack[stackPointer][8]; m21 = stack[stackPointer][9]; m22 = stack[stackPointer][10]; m23 = stack[stackPointer][11];
      m30 = stack[stackPointer][12]; m31 = stack[stackPointer][13]; m32 = stack[stackPointer][14]; m33 = stack[stackPointer][15];
      return true;
    }

    void set(float m00, float m01, float m02, float m03,
             float m10, float m11, float m12, float m13,
             float m20, float m21, float m22, float m23,
             float m30, float m31, float m32, float m33) {
      this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
      this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
      this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
      this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;

    }


    public void translate(float tx, float ty) {
      translate(tx, ty, 0);
    }

    public void invTranslate(float tx, float ty) {
      invTranslate(tx, ty, 0);
    }


    public void translate(float tx, float ty, float tz) {
      m03 += tx*m00 + ty*m01 + tz*m02;
      m13 += tx*m10 + ty*m11 + tz*m12;
      m23 += tx*m20 + ty*m21 + tz*m22;
      m33 += tx*m30 + ty*m31 + tz*m32;
    }

    public void invTranslate(float tx, float ty, float tz) {
      preApplyMatrix(1, 0, 0, -tx,
          0, 1, 0, -ty,
          0, 0, 1, -tz,
          0, 0, 0, 1);
    }


    // OPT could save several multiplies for the 0s and 1s by just
    //     putting the multMatrix code here and removing uneccessary terms

    public void rotateX(float angle) {
      //rotate(angle, 1, 0, 0);
      float c = cos(angle);
      float s = sin(angle);
      applyMatrix(1, 0, 0, 0,  0, c, -s, 0,  0, s, c, 0,  0, 0, 0, 1);
    }

    public void invRotateX(float angle) {
      float c = cos(-angle);
      float s = sin(-angle);
      preApplyMatrix(1, 0, 0, 0,  0, c, -s, 0,  0, s, c, 0,  0, 0, 0, 1);
    }


    public void rotateY(float angle) {
      //rotate(angle, 0, 1, 0);
      float c = cos(angle);
      float s = sin(angle);
      applyMatrix(c, 0, s, 0,  0, 1, 0, 0,  -s, 0, c, 0,  0, 0, 0, 1);
    }

    public void invRotateY(float angle) {
      //rotate(angle, 0, 1, 0);
      float c = cos(-angle);
      float s = sin(-angle);
      preApplyMatrix(c, 0, s, 0,  0, 1, 0, 0,  -s, 0, c, 0,  0, 0, 0, 1);
    }

    // two dimensional rotation is the same as rotating along the z-axis
    public void rotate(float angle) {
      rotateZ(angle);
    }

    public void invRotate(float angle) {
      invRotateZ(angle);
    }

    // note that this doesn't make things 3D
    public void rotateZ(float angle) {
      //rotate(angle, 0, 0, 1);
      float c = cos(angle);
      float s = sin(angle);
      applyMatrix(c, -s, 0, 0,  s, c, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
    }

    public void invRotateZ(float angle) {
      //rotate(angle, 0, 0, 1);
      float c = cos(-angle);
      float s = sin(-angle);
      preApplyMatrix(c, -s, 0, 0,  s, c, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
    }



    public void rotate(float angle, float v0, float v1, float v2) {
      // should be in radians (i think), instead of degrees (gl uses degrees)
      // based on 15-463 code, but similar to opengl ref p.443

      // TODO should make sure this vector is normalized

      float c = cos(angle);
      float s = sin(angle);
      float t = 1.0f - c;

      applyMatrix((t*v0*v0) + c, (t*v0*v1) - (s*v2), (t*v0*v2) + (s*v1), 0,
                  (t*v0*v1) + (s*v2), (t*v1*v1) + c, (t*v1*v2) - (s*v0), 0,
                  (t*v0*v2) - (s*v1), (t*v1*v2) + (s*v0), (t*v2*v2) + c, 0,
                  0, 0, 0, 1);
    }

    public void invRotate(float angle, float v0, float v1, float v2) {
      // TODO should make sure this vector is normalized

      float c = cos(-angle);
      float s = sin(-angle);
      float t = 1.0f - c;

      preApplyMatrix((t*v0*v0) + c, (t*v0*v1) - (s*v2), (t*v0*v2) + (s*v1), 0,
          (t*v0*v1) + (s*v2), (t*v1*v1) + c, (t*v1*v2) - (s*v0), 0,
          (t*v0*v2) - (s*v1), (t*v1*v2) + (s*v0), (t*v2*v2) + c, 0,
          0, 0, 0, 1);
    }

    public void scale(float s) {
      applyMatrix(s, 0, 0, 0,  0, s, 0, 0,  0, 0, s, 0,  0, 0, 0, 1);
    }

    public void invScale(float s) {
      preApplyMatrix(1/s, 0, 0, 0,  0, 1/s, 0, 0,  0, 0, 1/s, 0,  0, 0, 0, 1);
    }

    public void scale(float sx, float sy) {
      applyMatrix(sx, 0, 0, 0,  0, sy, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
    }

    public void invScale(float sx, float sy) {
      preApplyMatrix(1/sx, 0, 0, 0,  0, 1/sy, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
    }

    // OPTIMIZE: same as above
    public void scale(float x, float y, float z) {
      applyMatrix(x, 0, 0, 0,  0, y, 0, 0,  0, 0, z, 0,  0, 0, 0, 1);
    }

    public void invScale(float x, float y, float z) {
      preApplyMatrix(1/x, 0, 0, 0,  0, 1/y, 0, 0,  0, 0, 1/z, 0,  0, 0, 0, 1);
    }

    public void transform(float n00, float n01, float n02, float n03,
                          float n10, float n11, float n12, float n13,
                          float n20, float n21, float n22, float n23,
                          float n30, float n31, float n32, float n33) {
      applyMatrix(n00, n01, n02, n03,  n10, n11, n12, n13,
          n20, n21, n22, n23,  n30, n31, n32, n33);
    }

    public void preApplyMatrix(PMatrix lhs) {
      preApplyMatrix(lhs.m00, lhs.m01, lhs.m02, lhs.m03,
                     lhs.m10, lhs.m11, lhs.m12, lhs.m13,
                     lhs.m20, lhs.m21, lhs.m22, lhs.m23,
                     lhs.m30, lhs.m31, lhs.m32, lhs.m33);
    }

    // for inverse operations, like multiplying the matrix on the left
    public void preApplyMatrix(float n00, float n01, float n02, float n03,
                               float n10, float n11, float n12, float n13,
                               float n20, float n21, float n22, float n23,
                               float n30, float n31, float n32, float n33) {
      //modelMatrixIsIdentity = false;

      float r00 = n00*m00 + n01*m10 + n02*m20 + n03*m30;
      float r01 = n00*m01 + n01*m11 + n02*m21 + n03*m31;
      float r02 = n00*m02 + n01*m12 + n02*m22 + n03*m32;
      float r03 = n00*m03 + n01*m13 + n02*m23 + n03*m33;

      float r10 = n10*m00 + n11*m10 + n12*m20 + n13*m30;
      float r11 = n10*m01 + n11*m11 + n12*m21 + n13*m31;
      float r12 = n10*m02 + n11*m12 + n12*m22 + n13*m32;
      float r13 = n10*m03 + n11*m13 + n12*m23 + n13*m33;

      float r20 = n20*m00 + n21*m10 + n22*m20 + n23*m30;
      float r21 = n20*m01 + n21*m11 + n22*m21 + n23*m31;
      float r22 = n20*m02 + n21*m12 + n22*m22 + n23*m32;
      float r23 = n20*m03 + n21*m13 + n22*m23 + n23*m33;

      float r30 = n30*m00 + n31*m10 + n32*m20 + n33*m30;
      float r31 = n30*m01 + n31*m11 + n32*m21 + n33*m31;
      float r32 = n30*m02 + n31*m12 + n32*m22 + n33*m32;
      float r33 = n30*m03 + n31*m13 + n32*m23 + n33*m33;

      m00 = r00; m01 = r01; m02 = r02; m03 = r03;
      m10 = r10; m11 = r11; m12 = r12; m13 = r13;
      m20 = r20; m21 = r21; m22 = r22; m23 = r23;
      m30 = r30; m31 = r31; m32 = r32; m33 = r33;
    }

    public boolean invApplyMatrix(PMatrix rhs) {
      PMatrix copy = new PMatrix(rhs);
      PMatrix inverse = copy.invert();
      if (inverse == null) return false;
      preApplyMatrix(inverse);
      return true;
    }

    public boolean invApplyMatrix(float n00, float n01, float n02, float n03,
                                  float n10, float n11, float n12, float n13,
                                  float n20, float n21, float n22, float n23,
                                  float n30, float n31, float n32, float n33) {
      PMatrix copy = new PMatrix(0, n00, n01, n02, n03,
                                    n10, n11, n12, n13,
                                    n20, n21, n22, n23,
                                    n30, n31, n32, n33);
      PMatrix inverse = copy.invert();
      if (inverse == null) return false;
      preApplyMatrix(inverse);
      return true;
    }

    public void applyMatrix(PMatrix rhs) {
      applyMatrix(rhs.m00, rhs.m01, rhs.m02, rhs.m03,
                  rhs.m10, rhs.m11, rhs.m12, rhs.m13,
                  rhs.m20, rhs.m21, rhs.m22, rhs.m23,
                  rhs.m30, rhs.m31, rhs.m32, rhs.m33);
    }

    public void applyMatrix(float n00, float n01, float n02, float n03,
                            float n10, float n11, float n12, float n13,
                            float n20, float n21, float n22, float n23,
                            float n30, float n31, float n32, float n33) {
      //modelMatrixIsIdentity = false;

      float r00 = m00*n00 + m01*n10 + m02*n20 + m03*n30;
      float r01 = m00*n01 + m01*n11 + m02*n21 + m03*n31;
      float r02 = m00*n02 + m01*n12 + m02*n22 + m03*n32;
      float r03 = m00*n03 + m01*n13 + m02*n23 + m03*n33;

      float r10 = m10*n00 + m11*n10 + m12*n20 + m13*n30;
      float r11 = m10*n01 + m11*n11 + m12*n21 + m13*n31;
      float r12 = m10*n02 + m11*n12 + m12*n22 + m13*n32;
      float r13 = m10*n03 + m11*n13 + m12*n23 + m13*n33;

      float r20 = m20*n00 + m21*n10 + m22*n20 + m23*n30;
      float r21 = m20*n01 + m21*n11 + m22*n21 + m23*n31;
      float r22 = m20*n02 + m21*n12 + m22*n22 + m23*n32;
      float r23 = m20*n03 + m21*n13 + m22*n23 + m23*n33;

      float r30 = m30*n00 + m31*n10 + m32*n20 + m33*n30;
      float r31 = m30*n01 + m31*n11 + m32*n21 + m33*n31;
      float r32 = m30*n02 + m31*n12 + m32*n22 + m33*n32;
      float r33 = m30*n03 + m31*n13 + m32*n23 + m33*n33;

      m00 = r00; m01 = r01; m02 = r02; m03 = r03;
      m10 = r10; m11 = r11; m12 = r12; m13 = r13;
      m20 = r20; m21 = r21; m22 = r22; m23 = r23;
      m30 = r30; m31 = r31; m32 = r32; m33 = r33;
    }

    void mult3(float vec[], float out[]) {
      // Must use these temp vars because vec may be the same as out
      tmpx = m00*vec[0] + m01*vec[1] + m02*vec[2] + m03;
      tmpy = m10*vec[0] + m11*vec[1] + m12*vec[2] + m13;
      tmpz = m20*vec[0] + m21*vec[1] + m22*vec[2] + m23;

      out[0] = tmpx;
      out[1] = tmpy;
      out[2] = tmpz;
    }

    void mult(float vec[], float out[]) {
      // Must use these temp vars because vec may be the same as out
      tmpx = m00*vec[0] + m01*vec[1] + m02*vec[2] + m03*vec[3];
      tmpy = m10*vec[0] + m11*vec[1] + m12*vec[2] + m13*vec[3];
      tmpz = m20*vec[0] + m21*vec[1] + m22*vec[2] + m23*vec[3];
      tmpw = m30*vec[0] + m31*vec[1] + m32*vec[2] + m33*vec[3];

      out[0] = tmpx;
      out[1] = tmpy;
      out[2] = tmpz;
      out[3] = tmpw;
    }

    /**
     * @return the determinant of the matrix
     */
    public float determinant() {
      float f =
          m00
          * ((m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32)
          - m13 * m22 * m31
          - m11 * m23 * m32
          - m12 * m21 * m33);
      f -= m01
          * ((m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32)
          - m13 * m22 * m30
          - m10 * m23 * m32
          - m12 * m20 * m33);
      f += m02
          * ((m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31)
          - m13 * m21 * m30
          - m10 * m23 * m31
          - m11 * m20 * m33);
      f -= m03
          * ((m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31)
          - m12 * m21 * m30
          - m10 * m22 * m31
          - m11 * m20 * m32);
      return f;
    }

    /**
     * Calculate the determinant of a 3x3 matrix
     * @return result
     */

    private float determinant3x3(float t00, float t01, float t02,
                                 float t10, float t11, float t12,
                                 float t20, float t21, float t22)
    {
      return   t00 * (t11 * t22 - t12 * t21)
          + t01 * (t12 * t20 - t10 * t22)
          + t02 * (t10 * t21 - t11 * t20);
    }

    public PMatrix transpose() {
      float temp;
      temp = m01; m01 = m10; m10 = temp;
      temp = m02; m02 = m20; m20 = temp;
      temp = m03; m03 = m30; m30 = temp;
      temp = m12; m12 = m21; m21 = temp;
      temp = m13; m13 = m31; m31 = temp;
      temp = m23; m23 = m32; m32 = temp;
      return this;
    }

    /**
     * Invert this matrix
     * @return this if successful, null otherwise
     */
    public PMatrix invert() {

      float determinant = determinant();

      if (determinant != 0)
      {
        /*
        * m00 m01 m02 m03
        * m10 m11 m12 m13
        * m20 m21 m22 m23
        * m30 m31 m32 m33
        */
        float determinant_inv = 1f/determinant;

        // first row
        float t00 =  determinant3x3(m11, m12, m13, m21, m22, m23, m31, m32, m33);
        float t01 = -determinant3x3(m10, m12, m13, m20, m22, m23, m30, m32, m33);
        float t02 =  determinant3x3(m10, m11, m13, m20, m21, m23, m30, m31, m33);
        float t03 = -determinant3x3(m10, m11, m12, m20, m21, m22, m30, m31, m32);
        // second row
        float t10 = -determinant3x3(m01, m02, m03, m21, m22, m23, m31, m32, m33);
        float t11 =  determinant3x3(m00, m02, m03, m20, m22, m23, m30, m32, m33);
        float t12 = -determinant3x3(m00, m01, m03, m20, m21, m23, m30, m31, m33);
        float t13 =  determinant3x3(m00, m01, m02, m20, m21, m22, m30, m31, m32);
        // third row
        float t20 =  determinant3x3(m01, m02, m03, m11, m12, m13, m31, m32, m33);
        float t21 = -determinant3x3(m00, m02, m03, m10, m12, m13, m30, m32, m33);
        float t22 =  determinant3x3(m00, m01, m03, m10, m11, m13, m30, m31, m33);
        float t23 = -determinant3x3(m00, m01, m02, m10, m11, m12, m30, m31, m32);
        // fourth row
        float t30 = -determinant3x3(m01, m02, m03, m11, m12, m13, m21, m22, m23);
        float t31 =  determinant3x3(m00, m02, m03, m10, m12, m13, m20, m22, m23);
        float t32 = -determinant3x3(m00, m01, m03, m10, m11, m13, m20, m21, m23);
        float t33 =  determinant3x3(m00, m01, m02, m10, m11, m12, m20, m21, m22);

        // transpose and divide by the determinant
        m00 = t00*determinant_inv;
        m11 = t11*determinant_inv;
        m22 = t22*determinant_inv;
        m33 = t33*determinant_inv;
        m01 = t10*determinant_inv;
        m10 = t01*determinant_inv;
        m20 = t02*determinant_inv;
        m02 = t20*determinant_inv;
        m12 = t21*determinant_inv;
        m21 = t12*determinant_inv;
        m03 = t30*determinant_inv;
        m30 = t03*determinant_inv;
        m13 = t31*determinant_inv;
        m31 = t13*determinant_inv;
        m32 = t23*determinant_inv;
        m23 = t32*determinant_inv;
        return this;
      } else
        return null;
    }

    public void print() {
      int big = (int) Math.abs(max(max(max(max(abs(m00), abs(m01)),
                                           max(abs(m02), abs(m03))),
                                       max(max(abs(m10), abs(m11)),
                                           max(abs(m12), abs(m13)))),
                                   max(max(max(abs(m20), abs(m21)),
                                           max(abs(m22), abs(m23))),
                                       max(max(abs(m30), abs(m31)),
                                           max(abs(m32), abs(m33))))));
      int d = 1;
      while ((big /= 10) != 0) d++;  // cheap log()

      System.out.println(PApplet.nfs(m00, d, 4) + " " +
                         PApplet.nfs(m01, d, 4) + " " +
                         PApplet.nfs(m02, d, 4) + " " +
                         PApplet.nfs(m03, d, 4));

      System.out.println(PApplet.nfs(m10, d, 4) + " " +
                         PApplet.nfs(m11, d, 4) + " " +
                         PApplet.nfs(m12, d, 4) + " " +
                         PApplet.nfs(m13, d, 4));

      System.out.println(PApplet.nfs(m20, d, 4) + " " +
                         PApplet.nfs(m21, d, 4) + " " +
                         PApplet.nfs(m22, d, 4) + " " +
                         PApplet.nfs(m23, d, 4));

      System.out.println(PApplet.nfs(m30, d, 4) + " " +
                         PApplet.nfs(m31, d, 4) + " " +
                         PApplet.nfs(m32, d, 4) + " " +
                         PApplet.nfs(m33, d, 4));

      System.out.println();
    }

    private final float max(float a, float b) {
      return (a > b) ? a : b;
    }

    private final float abs(float a) {
      return (a < 0) ? -a : a;
    }

    private final float sin(float angle) {
      if (angleMode == DEGREES) angle *= DEG_TO_RAD;
      return (float)Math.sin(angle);
    }

    private final float cos(float angle) {
      if (angleMode == DEGREES) angle *= DEG_TO_RAD;
      return (float)Math.cos(angle);
    }
}
#include <PinChangeInterrupt.h>

const int TPIN_T = 2;
const int EPIN_T = 3;
const int TPIN_B = 4;
const int EPIN_B = 5;
const int TPIN_L = 6;
const int EPIN_L = 7;
const int TPIN_R = 8;
const int EPIN_R = 9;

const int LED_T = 10;
const int LED_B = 11;
const int LED_L = 12;
const int LED_R = 13;
const int LED_CLOSE = 1;

const int THRESHOLD_NEAR = 25;
const int THRESHOLD_FAR = 25;
const int THRESHOLD_NOTHING = 120;

const int QUEUE_SIZE = 120;

const int NEAR = 1;
const int FAR = 2;
const int NOTHING = 3;

const int DISTANCE_NEAR = 10;
const int DISTANCE_FAR = 70;

const int LEFT = 0;
const int RIGHT = 1;
const int TOP = 2;
const int BOTTOM = 3;
const int HORIZONTAL = 98;
const int VERTICAL = 99;
const int MAX_GC = 1000;

unsigned long echo_durationL = 0;
unsigned long echo_durationR = 0;
unsigned long echo_durationT = 0;
unsigned long echo_durationB = 0;

bool isOn = false;

int stateLeft = 0;
int stateRight = 0;
int stateTop = 0;
int stateBottom = 0;
int stateQL[QUEUE_SIZE];
int stateQR[QUEUE_SIZE];
int stateQT[QUEUE_SIZE];
int stateQB[QUEUE_SIZE];

unsigned long gestureCountL, gestureCountR, gestureCountT, gestureCountB = 0;
typedef struct _WaveState {
  int prev;
  int now;
} WaveState;
WaveState leftS, rightS, topS, bottomS;

void echoISR_L() {
  static unsigned long echo_begin = 0;
  static unsigned long echo_end = 0;
  unsigned int echo_pin_state = digitalRead(EPIN_L);
  if (echo_pin_state == HIGH) {
    echo_begin = micros();
  } else {
    echo_end = micros();
    echo_durationL = echo_end - echo_begin;
  }
}

void echoISR_R() {
  static unsigned long echo_begin = 0;
  static unsigned long echo_end = 0;
  unsigned int echo_pin_state = digitalRead(EPIN_R);
  if (echo_pin_state == HIGH) {
    echo_begin = micros();
  } else {
    echo_end = micros();
    echo_durationR = echo_end - echo_begin;
  }
}

void echoISR_T() {
  static unsigned long echo_begin = 0;
  static unsigned long echo_end = 0;
  unsigned int echo_pin_state = digitalRead(EPIN_T);
  if (echo_pin_state == HIGH) {
    echo_begin = micros();
  } else {
    echo_end = micros();
    echo_durationT = echo_end - echo_begin;
  }
}

void echoISR_B() {
  static unsigned long echo_begin = 0;
  static unsigned long echo_end = 0;
  unsigned int echo_pin_state = digitalRead(EPIN_B);
  if (echo_pin_state == HIGH) {
    echo_begin = micros();
  } else {
    echo_end = micros();
    echo_durationB = echo_end - echo_begin;
  }
}

void setQEmpty(int *Q) {
  int i;
  for (i = 0; i < QUEUE_SIZE; i++) {
    Q[i] = 0;
  }
}

void pushToQue(int *stateQ, int cState) {
  int i;
  for (i = 0; i < QUEUE_SIZE; i++) {
    if (stateQ[i] == 0) {
      stateQ[i] = cState;
      return;
    }
  }
  for (i = 0; i < QUEUE_SIZE - 1; i++) {
    stateQ[i] = stateQ[i + 1];
  }
  stateQ[QUEUE_SIZE - 1] = cState;
}

int getCurrentState(int *stateQ) {
  int i;
  int near = 0, far = 0, nothing = 0;
  for (i = 0; i < QUEUE_SIZE; i++) {
    if (stateQ[i] == NEAR) {
      near += 1;
    } else if (stateQ[i] == FAR) {
      far += 1;
    } else if (stateQ[i] == NOTHING) {
      nothing += 1;
    }
  }

  if (near > THRESHOLD_NEAR) {
    setQEmpty(stateQ);
    return NEAR;
  } else if (far > THRESHOLD_FAR) {
    setQEmpty(stateQ);
    return FAR;
  } else if (nothing > THRESHOLD_NOTHING) {
    setQEmpty(stateQ);
    return NOTHING;
  }
  return -1;
}

int checkDistanceAndGetState(long d, int *stateQ) {
  int changingState;
  int result;
  if (d <= 0) {
    return -1;
  }
  if (d < DISTANCE_NEAR) {
    changingState = NEAR;
  } else if (d < DISTANCE_FAR) {
    changingState = FAR;
  } else {
    changingState = NOTHING;
  }
  pushToQue(stateQ, changingState);
  result = getCurrentState(stateQ);
  return result;
}

int getDistance(int TPIN, int echo_duration, int dir) {
  if (echo_duration == 0) {
    digitalWrite(TPIN, LOW);
    delayMicroseconds(2);
    digitalWrite(TPIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TPIN, LOW);
  } else {
    long distance = echo_duration / 58;
    if (dir == LEFT) echo_durationL = 0;
    else if (dir == RIGHT) echo_durationR = 0;
    else if (dir == TOP) {
//      Serial.print(distance);
//      Serial.print("\n");
      echo_durationT = 0;
    }
    else if (dir == BOTTOM) {
      echo_durationB = 0;
    }
    return distance;
  }
  return -1;
}

void getDistanceAndSetStates() {
  long dL, dR, dT, dB;
  int newState;

  dL = getDistance(TPIN_L, echo_durationL, LEFT);
  dR = getDistance(TPIN_R, echo_durationR, RIGHT);
  newState = checkDistanceAndGetState(dL, stateQL);
  if (newState > 0) {
//    Serial.print("왼쪽_");
//    Serial.print(newState);
//    Serial.print("\n");
    leftS.prev = leftS.now;
    leftS.now = newState;
  }
  newState = checkDistanceAndGetState(dR, stateQR);
  if (newState > 0) {
//    Serial.print("오른쪽_");
//    Serial.print(newState);
//    Serial.print("\n");
    rightS.prev = rightS.now;
    rightS.now = newState;
  }
 
  dT = getDistance(TPIN_T, echo_durationT, TOP);
  dB = getDistance(TPIN_B, echo_durationB, BOTTOM);
  newState = checkDistanceAndGetState(dT, stateQT);
  if (newState > 0) {
    //Serial.print("위_");
    //Serial.print(newState);
    //Serial.print("\n");
    topS.prev = topS.now;
    topS.now = newState;
  }
  newState = checkDistanceAndGetState(dB, stateQB);
  if (newState > 0 ) {
    //Serial.print("아래_");
    //Serial.print(newState);
    //Serial.print("\n");
    bottomS.prev = bottomS.now;
    bottomS.now = newState;
  }
}

int checkGesture(int dir, WaveState s1, WaveState s2) {
  if (s1.prev == NEAR && s1.now == FAR) {
    if (dir == HORIZONTAL && gestureCountL > MAX_GC * 4) {
      gestureCountL = 0;
      Serial.print("Left near->far clear\n");
    } else if (dir == VERTICAL && gestureCountT > MAX_GC * 4) {
      gestureCountT = 0;
      Serial.print("Top near->far clear\n");
    }
  } else if (s2.prev == NEAR && s2.now == FAR) {
    if (dir == HORIZONTAL && gestureCountR > MAX_GC * 4) {
      Serial.print("Right near->far clear\n");
      gestureCountR = 0;
    } else if (dir == VERTICAL && gestureCountB > MAX_GC * 4) {
      Serial.print("Bottom near->far clear\n");
      gestureCountB = 0;
    }
  }

  if (s2.now == NEAR) {
    if (dir == HORIZONTAL && gestureCountL < MAX_GC) {
      Serial.print(gestureCountL);
      gestureCountL = MAX_GC;
      gestureCountR = MAX_GC;
      digitalWrite(LED_L, HIGH);
      delay(500);
      digitalWrite(LED_L, LOW);
      Serial.print("왼쪽 화면으로 이동\n");
      return LEFT;
    } else if (dir == VERTICAL && gestureCountT < MAX_GC) {
      gestureCountT = MAX_GC;
      gestureCountB = MAX_GC;
      digitalWrite(LED_T, HIGH);
      delay(500);
      digitalWrite(LED_T, LOW);
      Serial.print("위 화면으로 이동\n");
      return TOP;
    }
  } else if (s1.now == NEAR) {
    if (dir == HORIZONTAL && gestureCountR < MAX_GC) {
      Serial.print(gestureCountR);
      gestureCountL = MAX_GC;
      gestureCountR = MAX_GC;
      digitalWrite(LED_R, HIGH);
      delay(500);
      digitalWrite(LED_R, LOW);
      Serial.print("오른쪽 화면으로 이동\n");
      return RIGHT;
    } else if (dir == VERTICAL && gestureCountB < MAX_GC) {
      gestureCountT = MAX_GC;
      gestureCountB = MAX_GC;
      digitalWrite(LED_B, HIGH);
      delay(500);
      digitalWrite(LED_B, LOW);
      Serial.print("아래 화면으로 이동\n");
      return BOTTOM;
    }
  }
  if (dir == HORIZONTAL) {
    gestureCountL++;
    gestureCountR++;
  } else if (dir == VERTICAL) {
    gestureCountT++;
    gestureCountB++;
  }
  return -1;
}

bool isClose(WaveState bottomS){
  if (bottomS.now == FAR || bottomS.now == NEAR) {
    if (!isOn) {
      Serial.print("화면 ON!");
      Serial.print("\n");
      isOn = true;
      digitalWrite(LED_CLOSE, HIGH);
    }
  } else if(bottomS.now == NOTHING && isOn) {
    Serial.print("화면 OFF!");
    Serial.print("\n");
    isOn = false;
    digitalWrite(LED_CLOSE, LOW);
  }
  return isOn;
}

void setup() {
  pinMode(TPIN_L, OUTPUT);
  pinMode(EPIN_L, INPUT);
  pinMode(TPIN_R, OUTPUT);
  pinMode(EPIN_R, INPUT);
  pinMode(TPIN_T, OUTPUT);
  pinMode(EPIN_T, INPUT);
  pinMode(TPIN_B, OUTPUT);
  pinMode(EPIN_B, INPUT);
  pinMode(LED_L, OUTPUT);
  pinMode(LED_R, OUTPUT);
  pinMode(LED_T, OUTPUT);
  pinMode(LED_B, OUTPUT);
  pinMode(LED_CLOSE, OUTPUT);
  Serial.begin(115200);
  attachPCINT(digitalPinToPCINT(EPIN_L), echoISR_L, CHANGE);
  attachPCINT(digitalPinToPCINT(EPIN_R), echoISR_R, CHANGE);
  attachPCINT(digitalPinToPCINT(EPIN_T), echoISR_T, CHANGE);
  attachPCINT(digitalPinToPCINT(EPIN_B), echoISR_B, CHANGE);
}

void loop() {
  getDistanceAndSetStates();
  if (isClose(bottomS)) {
    if (checkGesture(HORIZONTAL, leftS, rightS) >= 0) {
      leftS.prev = 0; leftS.now = 0;
      rightS.prev = 0; rightS.now = 0;
    }
    if (checkGesture(VERTICAL, topS, bottomS) >= 0) {
      topS.prev = 0; topS.now = 0;
      bottomS.prev = 0; bottomS.now = 0;
    }
  }
}

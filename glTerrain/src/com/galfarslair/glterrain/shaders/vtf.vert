//#define DRAW_EDGES

attribute vec2 position;
attribute vec3 baryAttribs;

uniform float terrainSize;
uniform float tileSize;
uniform float nodeScale;
uniform vec2 nodePos;
uniform mat4 matProjView;
uniform vec3 cameraPos;

uniform float heightSampleScale;
uniform vec4 lodScales;

uniform sampler2D texHeight;

varying vec2 groundTexCoords;
varying vec2 detailTexCoords;

#ifdef DRAW_EDGES
varying vec3 baryCoords;
varying vec2 nodeCoords;
#endif

const float detailScale = 0.5;
const float eps = 0.001;

float moveEdgeCoord(float edgeCoord, float lodScale) {
  float m = mod(edgeCoord, lodScale);
  return step(0.5, m / lodScale) * lodScale + (edgeCoord - m);
}

void main() {
#ifdef DRAW_EDGES    
    baryCoords = baryAttribs;
    nodeCoords = position / tileSize;
#endif

    vec2 p = position;

    if (p.x < eps) {
        p.y = moveEdgeCoord(p.y, lodScales[0]);
    } else if (p.x > tileSize - eps) {
        p.y = moveEdgeCoord(p.y, lodScales[2]);           
    }   
    if (p.y < eps) {
        p.x = moveEdgeCoord(p.x, lodScales[1]);        
    } else if (p.y > tileSize - eps) {
        p.x = moveEdgeCoord(p.x, lodScales[3]);
    }
            
    vec3 pos = vec3(p.x * nodeScale + nodePos.x, p.y * nodeScale + nodePos.y, 0.0);    
    groundTexCoords = vec2(pos.x / terrainSize, pos.y / terrainSize);
    
    pos.z = texture2DLod(texHeight, groundTexCoords, 0.0).a * heightSampleScale;    

    detailTexCoords = vec2(pos.x * detailScale, pos.y * detailScale);  
    gl_Position = matProjView * vec4(pos, 1.0);
}




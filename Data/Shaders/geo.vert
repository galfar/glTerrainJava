#define DRAW_EDGES

attribute vec2 position;
attribute float height;
attribute vec3 baryAttribs;

uniform float terrainSize;
uniform float nodeSize;
uniform vec2 nodePos;
uniform mat4 matProjView;

varying vec2 groundTexCoords;
varying vec2 detailTexCoords;
#ifdef DRAW_EDGES
varying vec3 baryCoords;
varying vec2 nodeCoords;
#endif

const float detailScale = 0.25;

void main() {
#ifdef DRAW_EDGES    
    baryCoords = baryAttribs;
    nodeCoords = position / nodeSize;
#endif
    vec3 pos = vec3(position.x + nodePos.x, position.y + nodePos.y, height);  
    groundTexCoords = vec2(pos.x / terrainSize, pos.y / terrainSize);
    detailTexCoords = vec2(pos.x * detailScale, pos.y * detailScale);  
    gl_Position = matProjView * vec4(pos, 1.0);
}
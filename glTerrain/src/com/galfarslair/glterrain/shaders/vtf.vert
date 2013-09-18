//#define DRAW_EDGES

attribute vec2 position;
attribute vec3 baryAttribs;

uniform float terrainSize;
uniform float nodeSize;
uniform vec2 nodePos;
uniform mat4 matProjView;
uniform vec3 cameraPos;

uniform float heightSampleScale;

uniform sampler2D texHeight;

varying vec2 groundTexCoords;
varying vec2 detailTexCoords;

#ifdef DRAW_EDGES
varying vec3 baryCoords;
varying vec2 nodeCoords;
#endif

const float detailScale = 0.5;

void main() {
#ifdef DRAW_EDGES    
    baryCoords = baryAttribs;
    nodeCoords = position / nodeSize;
#endif

    vec3 pos = vec3(position.x + nodePos.x, position.y + nodePos.y, 0);

    
    groundTexCoords = vec2(pos.x / terrainSize, pos.y / terrainSize);
    
    pos.z = texture2DLod(texHeight, groundTexCoords, 0.0).a * heightSampleScale;    

    detailTexCoords = vec2(pos.x * detailScale, pos.y * detailScale);  
    gl_Position = matProjView * vec4(pos, 1.0);
}
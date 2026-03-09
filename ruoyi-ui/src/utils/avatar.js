/**
 * 根据昵称生成默认头像（Canvas 绘制，返回 base64）
 * @param {string} nickName 用户昵称
 * @param {number} size 图片尺寸（默认 80）
 * @returns {string} base64 图片 data URL
 */
export function generateAvatar(nickName, size = 80) {
  var canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  var ctx = canvas.getContext('2d')

  // 根据昵称生成固定颜色（同一昵称总是同一颜色）
  var colors = [
    '#1abc9c', '#2ecc71', '#3498db', '#9b59b6', '#34495e',
    '#16a085', '#27ae60', '#2980b9', '#8e44ad', '#2c3e50',
    '#f1c40f', '#e67e22', '#e74c3c', '#f39c12', '#d35400',
    '#1e90ff', '#ff6348', '#7bed9f', '#ff6b81', '#70a1ff'
  ]
  var charCode = 0
  for (var i = 0; i < nickName.length; i++) {
    charCode += nickName.charCodeAt(i)
  }
  var bgColor = colors[charCode % colors.length]

  // 绘制圆形背景
  ctx.fillStyle = bgColor
  ctx.beginPath()
  ctx.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2)
  ctx.fill()

  // 绘制文字（取昵称最后两个字，单字则取一个）
  var text = nickName.length >= 2 ? nickName.slice(-2) : nickName
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold ' + (size * 0.4) + 'px Arial, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(text, size / 2, size / 2)

  return canvas.toDataURL('image/png')
}
